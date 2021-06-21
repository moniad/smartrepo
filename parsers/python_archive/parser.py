import sys
import pika
import shutil
import os
import json
from zeep import Client
import logging
from confluent_kafka import Producer

storage = '/storage'
logging.basicConfig(format='%(asctime)s %(levelname)s - %(message)s', level=logging.INFO)
producer = None


def check_file_extension(file):
    wsdl = "http://smart-repo-backend:7777/ws/fileExtension.wsdl"
    try:
        client = Client(wsdl)
        response = client.service.getFileExtension(file)
        logging.info('RESPONSE FROM SOAP SERVER:', response)
    except:
        logging.error("Cannot connect to Soap server")
    return response


def callback(ch, method, properties, body):

    string_body = body.decode()

    logging.info(" [x] Received: %s" % string_body)
    producer.produce("parsers", key="video", value=f"Started parsing file {string_body}")

    orginal_path = storage + "/" + string_body
    temp_path = "__temp/" + string_body
    temp_path_abs = storage + "/" + temp_path

    shutil.unpack_archive(orginal_path, temp_path_abs)

    queue_id = ch.queue_declare(queue='').method.queue
    props = pika.spec.BasicProperties(reply_to=queue_id)

    ret = [{'name': string_body, 'path': orginal_path, 'content': ''}]

    for file in os.listdir(temp_path_abs):
        path = temp_path + "/" + file
        logging.info("FILE: ", file)

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

            logging.info("Parsed: " + file)
            logging.info("Result: " + ret_body.decode())
            producer.produce("parsers", key="video", value=f"Finished parsing file {file}")

            ret.append(
                {
                    'name': file,
                    'path': orginal_path + "/" + file,
                    'content': ret_body.decode()
                }
            )
        else:
            logging.warning("WARNING: " + file + " has inappropriate extension. It will not be processed.")
            producer.produce("parsers", key="video", value=f"Error parsing file {file}")

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

    conf = {'bootstrap.servers': rabbit_host+":9092"}
    producer = Producer(conf)

    connection = pika.BlockingConnection(pika.ConnectionParameters(rabbitmq_host, 5672))
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
