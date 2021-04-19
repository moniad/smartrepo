using log4net;
using System;
using System.Collections.Generic;
using Vosk;
using Optional;
using Optional.Collections;
using System.IO;

namespace VoskAudioParser
{
    class ModelsManager
    {
        private static readonly ILog log = LogManager.GetLogger(typeof(ModelsManager));

        //private static String baseDirectory = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "models");

        // use the absolute path to the models directory
        private static String baseDirectory = @"";

        private readonly Dictionary<SupportedLanguages, String> paths = new()
        {
            { SupportedLanguages.CA, Path.Combine(baseDirectory, "model_ca") },
            { SupportedLanguages.CN, Path.Combine(baseDirectory, "model_cn") },
            { SupportedLanguages.DE, Path.Combine(baseDirectory, "model_de") },
            { SupportedLanguages.EN, Path.Combine(baseDirectory, "model_en") },
            { SupportedLanguages.ES, Path.Combine(baseDirectory, "model_es") },
            { SupportedLanguages.FA, Path.Combine(baseDirectory, "model_fa") },
            { SupportedLanguages.FR, Path.Combine(baseDirectory, "model_fr") },
            { SupportedLanguages.IT, Path.Combine(baseDirectory, "model_it") },
            { SupportedLanguages.PT, Path.Combine(baseDirectory, "model_pt") },
            { SupportedLanguages.RU, Path.Combine(baseDirectory, "model_ru") },
            { SupportedLanguages.TR, Path.Combine(baseDirectory, "model_tr") },
            { SupportedLanguages.VN, Path.Combine(baseDirectory, "model_vn") }
        };

        private Dictionary<SupportedLanguages, Model> models = new();


        public Option<Model> GetModel(SupportedLanguages language)
        {
            if (models.TryGetValue(language, out Model model))
            {
                return Option.Some(model);
            }
            else
            {
                var newModel = paths.GetValueOrNone(language).Map(p => {
                    var model = new Model(p);
                    models.Add(language, model);
                    return model;
                });
                return newModel;
            }
        }

    }
}
