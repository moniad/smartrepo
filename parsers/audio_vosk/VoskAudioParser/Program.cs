using log4net;
using System;

namespace VoskAudioParser
{
    public class Program
    {
        private static readonly ILog Log = LogManager.GetLogger(typeof(Program));

        static void Main(string[] args)
        {
            Vosk.Vosk.SetLogLevel(0);

            ModelsManager manager = new();
            var language = SupportedLanguages.EN;
            var model = manager.GetModel(language);

            // define absolute path to audio file here (for now)
            var path = @"";

            model.Match(
                some: model => {
                    var parser = new AudioParser();
                    try {
                        var results = parser.ParseWaveFile(path, model);
                        Log.Info(results);
                    }
                    catch (Exception e)
                    {
                        Log.Error(e.Message);
                    }
                    
                },
                none: () => Log.Error($"Failed to load a model for language: {language}")
            );

        }
    }
}
