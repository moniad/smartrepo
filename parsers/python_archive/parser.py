import sys
import pika
import shutil
import os
import json
from zeep import Client

storage = '/storage'

def check_file_extension(file):
    wsdl = "http://smart-repo-backend:7777/ws/fileExtension.wsdl"
    response = None
    try:
        client = Client(wsdl)
        response = client.service.getFileExtension(file)
        print('RESPONSE FROM SOAP SERVER:', response)
    except:
        print("Cannot connect to Soap server")
    return response


def callback(ch, method, properties, body):

    string_body = body.decode()

    if string_body[0] == '/':
        string_body = string_body[1:]

    print(" [x] Received: %s" % string_body)

    original_path = storage + "/" + string_body
    temp_path = "__temp/" + string_body
    temp_path_abs = storage + "/" + temp_path

    shutil.unpack_archive(original_path, temp_path_abs)

    queue_id = ch.queue_declare(queue='').method.queue
    props = pika.spec.BasicProperties(reply_to=queue_id)

    ret = [{'name': string_body, 'extension': string_body.split('.')[-1], 'path': original_path, 'content': ''}]

    for file in os.listdir(temp_path_abs):
        path = temp_path + "/" + file
        print("FILE: ", file)

        relative_path = string_body + "/" + file
        extension = check_file_extension(relative_path)
        if extension is not None:
            ch.basic_publish(exchange='',
                             routing_key=extension,
                             body=path,
                             properties=props)

            method_frame = None
            ret_body = ''
            while method_frame is None:
                method_frame, _, ret_body = ch.basic_get(queue=queue_id)

            print("Parsed: " + file)
            print("Result: " + ret_body.decode())

            ret.append(
                {
                    'name': file,
                    'extension': extension,
                    'path': original_path + "/" + file,
                    'content': ret_body.decode()
                }
            )
        else:
            print("WARNING: " + file + " has inappropriate extension. It will not be processed.")

    ch.queue_delete(queue=queue_id)

    #TODO
    #temporary fix for finding file system related atributes for inner files - remove them after indexing on backend
    #shutil.rmtree(temp_path_abs, ignore_errors=True)

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

    params = pika.ConnectionParameters(host=rabbitmq_host, port=5672,
                                       heartbeat=600,
                                       blocked_connection_timeout=1000)
    connection = pika.BlockingConnection(params)
    channel = connection.channel()

    queues = ['tar', 'zip', 'gz']
    for queue in queues:
        channel.queue_declare(queue=queue)
        channel.basic_consume(queue=queue,
                              auto_ack=True,
                              on_message_callback=callback)

    channel.start_consuming()


if __name__=='__main__':
    main(sys.argv)
