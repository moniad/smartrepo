using log4net;
using NAudio.Wave;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text.Json;
using Vosk;

namespace VoskAudioParser
{
    class TextExtractor
    {
        private static readonly ILog log = LogManager.GetLogger(typeof(TextExtractor));

        public List<string> ExtractFromWaveFile(string path, Model model)
        {
            log.Info($"Processing file {path}");
            var results = new List<String>();
            var partialResults = new List<String>();

            using (var reader = new WaveFileReader(path))
            {

                if (!ProperFormat(reader.WaveFormat))
                {
                    log.Info($"Wrong format - Cannot extract data from file {path}");
                    return results;
                }

                int sampleRate = reader.WaveFormat.SampleRate;

                using var rec = new VoskRecognizer(model, sampleRate);

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = reader.Read(buffer, 0, buffer.Length)) > 0)
                {
                    if (rec.AcceptWaveform(buffer, bytesRead))
                        AddResult(rec.Result(), results, "text");
                    else
                        AddResult(rec.PartialResult(), partialResults, "partial");
                }

                AddResult(rec.FinalResult(), results, "text");
            }

            if (results.Any())
                return results;
            else
                return partialResults;
        }

        private bool ProperFormat(WaveFormat waveFormat)
        {
            return waveFormat.SampleRate >= FormatRequirements.minSamplingRate
                & waveFormat.Channels == FormatRequirements.maxChannelsNumber
                & waveFormat.BitsPerSample == FormatRequirements.bitsPerSample
                & waveFormat.Encoding.Equals(FormatRequirements.encoding);
        }

        private void AddResult(string fullResult, List<string> results, string property)
        {
            using var doc = JsonDocument.Parse(fullResult);
            string res = doc.RootElement.GetProperty(property).GetString();

            if (!res.Equals("")) results.Add(res);
        }

    }
}
