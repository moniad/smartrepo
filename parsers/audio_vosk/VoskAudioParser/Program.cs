using log4net;
using System;
using RabbitMQ.Client;
using RabbitMQ.Client.Events;
using System.Text;
using System.IO;
using System.Threading;

using System.Threading.Tasks;
using Confluent.Kafka;

namespace VoskAudioParser
{

    public class Program
    {
        private static readonly ILog Log = LogManager.GetLogger(typeof(Program));

        static void Main(string[] args)
        {

            string storagePath = "/storage";

            string rabbitHost = "localhost";
            int rabbitPort = 5672;

            if (args.Length > 0)
            {
                rabbitHost = args[0];
            }

            var config = new ProducerConfig
            {
                BootstrapServers = rabbitHost+":9092",
            };

            Vosk.Vosk.SetLogLevel(-1);

            ModelsManager manager = new();
            AudioParser parser = new();

            using var connection = RetryConnection(rabbitHost, rabbitPort);
            using (var channel = connection.CreateModel())
            {

                foreach (string extension in parser.AcceptedExtensions)
                {
                    Log.Debug(extension);
                    channel.QueueDeclare(queue: extension, durable: false, exclusive: false, autoDelete: false, arguments: null);
                }

                Log.Info("Waiting for messages");

                var consumer = new EventingBasicConsumer(channel);
                consumer.Received += async (model, ea) =>
                {
                    var replyTo = ea.BasicProperties.ReplyTo;

                    var body = ea.Body.ToArray();
                    var relativePath = Encoding.UTF8.GetString(body);
                    var path = Path.Join(storagePath, relativePath);

                    Log.Info($"Parsing {path}");
                    using (var producer = new ProducerBuilder<string, string>(config).Build())
                    {
                        try
                        {
                            var dr = await producer.ProduceAsync("parsers", new Message<string, string> {Key="audio", Value=$"Started parsing file {path}"});
                        }
                        catch (ProduceException<string, string> e)
                        {
                            Console.WriteLine($"Delivery failed: {e.Error.Reason}");
                        }
                    }

                    var language = SupportedLanguages.EN;
                    var languageModel = manager.GetModel(language);

                    string results = "";
                    languageModel.Match(
                        some: async model =>
                        {
                            try
                            {
                                results = parser.ParseWaveFile(path, model);
                                Log.Info($"Parsing results:\n{results}");
                                using (var producer = new ProducerBuilder<string, string>(config).Build())
                                {
                                    try
                                    {
                                        var dr = await producer.ProduceAsync("parsers", new Message<string, string> {Key="audio", Value=$"Finished parsing file {path}"});
                                    }
                                    catch (ProduceException<string, string> e)
                                    {
                                        Console.WriteLine($"Delivery failed: {e.Error.Reason}");
                                    }
                                }
                            }
                            catch (Exception e)
                            {
                                Log.Error(e.Message);
                                using (var producer = new ProducerBuilder<string, string>(config).Build())
                                {
                                    try
                                    {
                                        var dr = await producer.ProduceAsync("parsers", new Message<string, string> {Key="audio", Value=$"Error parsing file {path}"});
                                    }
                                    catch (ProduceException<string, string> pe)
                                    {
                                        Console.WriteLine($"Delivery failed: {pe.Error.Reason}");
                                    }
                                }
                            }

                        },
                        none: () => Log.Error($"Failed to load a model for language: {language}")
                    );

                    channel.BasicPublish(exchange: "", routingKey: replyTo, basicProperties: null, body: Encoding.UTF8.GetBytes(results));
                };

                foreach (string extension in parser.AcceptedExtensions)
                {
                    channel.BasicConsume(queue: extension, autoAck: true, consumer: consumer);
                }
                Thread.Sleep(Timeout.Infinite);
            }
        }

        private static IConnection RetryConnection(string rabbitHost, int rabbitPort)
        {
            var factory = new ConnectionFactory() { HostName = rabbitHost, Port = rabbitPort };

            int connectionTrials = 5;
            int waitTime = 5000;
            for (int retry = 0; retry <= connectionTrials; ++retry) {
                try
                {
                    Thread.Sleep(retry * waitTime);
                    return factory.CreateConnection();
                }
                catch (IOException)
                {
                    Log.Info("Unable to establish connection to RabbitMQ. Trial: " + retry);
                }
            }
            Log.Info("Unable to establish connection.");
            throw new Exception("No RabbitMQConnection provided.");
        }
    }
}
