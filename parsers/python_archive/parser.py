import sys
import pika
import shutil
import os
import json

storage = '/storage'

def callback(ch, method, properties, body):

    string_body = body.decode()

    print(" [x] Received %s" % string_body)

    orginal_path = storage + "/" + string_body
    temp_path = "__temp/" + string_body
    temp_path_abs = storage + "/" + temp_path

    shutil.unpack_archive(orginal_path, temp_path_abs)

    queue_id = ch.queue_declare(queue='').method.queue
    props = pika.spec.BasicProperties(reply_to=queue_id)

    ret = [{'name': string_body, 'path': orginal_path, 'content': ''}]

    for file in os.listdir(temp_path_abs):
        path = temp_path + "/" + file
        extension = path.split('.')[-1]

        ch.basic_publish(exchange='',
                         routing_key=extension,
                         body=path,
                         properties=props)

        method_frame = None
        ret_body = ''
        while method_frame is None:
            method_frame, _, ret_body = ch.basic_get(queue=queue_id)

        print("parsed: " + file)
        print("result: " + ret_body.decode())

        ret.append(
            {
                'name': file,
                'path': orginal_path + "/" + file,
                'content': ret_body.decode()
            }
        )

    ch.queue_delete(queue=queue_id)

    shutil.rmtree(temp_path_abs, ignore_errors=True)

    ch.basic_publish(exchange='',
                     routing_key=properties.reply_to,
                     properties=pika.BasicProperties(correlation_id = props.correlation_id),
                     body=json.dumps(ret)
                     )

def main(args):

    if len(args) > 1:
        rabbitmq_host = args[1]
    else:
        rabbitmq_host = 'localhost'

    connection = pika.BlockingConnection(pika.ConnectionParameters(rabbitmq_host, 5672))
    channel = connection.channel()

    queues = ['tar', 'zip']
    for queue in queues:
        channel.queue_declare(queue=queue)
        channel.basic_consume(queue=queue,
                              auto_ack=True,
                              on_message_callback=callback)

    channel.start_consuming()

if __name__=='__main__':
    main(sys.argv)