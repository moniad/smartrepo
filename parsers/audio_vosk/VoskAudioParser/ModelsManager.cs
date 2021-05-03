using log4net;
using System;
using System.Collections.Generic;
using Vosk;
using Optional;
using Optional.Collections;
using System.IO;

namespace VoskAudioParser
{
    public class ModelsManager
    {
        private static readonly ILog Log = LogManager.GetLogger(typeof(ModelsManager));

        private static readonly String BaseDirectory = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "models");

        private Dictionary<SupportedLanguages, Model> Models = new();

        private readonly Dictionary<SupportedLanguages, String> Paths = new()
        {
            { SupportedLanguages.CA, Path.Combine(BaseDirectory, "model_ca") },
            { SupportedLanguages.CN, Path.Combine(BaseDirectory, "model_cn") },
            { SupportedLanguages.DE, Path.Combine(BaseDirectory, "model_de") },
            { SupportedLanguages.EN, Path.Combine(BaseDirectory, "model_en") },
            { SupportedLanguages.ES, Path.Combine(BaseDirectory, "model_es") },
            { SupportedLanguages.FA, Path.Combine(BaseDirectory, "model_fa") },
            { SupportedLanguages.FR, Path.Combine(BaseDirectory, "model_fr") },
            { SupportedLanguages.IT, Path.Combine(BaseDirectory, "model_it") },
            { SupportedLanguages.PT, Path.Combine(BaseDirectory, "model_pt") },
            { SupportedLanguages.RU, Path.Combine(BaseDirectory, "model_ru") },
            { SupportedLanguages.TR, Path.Combine(BaseDirectory, "model_tr") },
            { SupportedLanguages.VN, Path.Combine(BaseDirectory, "model_vn") }
        };

        public Option<Model> GetModel(SupportedLanguages language)
        {
            if (Models.TryGetValue(language, out Model model))
            {
                return Option.Some(model);
            }
            else
            {
                var newModel = Paths.GetValueOrNone(language).Map(p =>
                {
                    var model = new Model(p);
                    Models.Add(language, model);
                    return model;
                });
                return newModel;
            }
        }
    }
}
