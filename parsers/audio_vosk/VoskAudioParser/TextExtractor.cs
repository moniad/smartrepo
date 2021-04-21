using log4net;
using NAudio.Wave;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text.Json;
using Vosk;

namespace VoskAudioParser
{
    public class TextExtractor
    {
        private static readonly ILog Log = LogManager.GetLogger(typeof(TextExtractor));

        public List<string> ExtractFromWaveFile(string path, Model model)
        {
            Log.Info($"Processing file {path}");
            var results = new List<String>();
            var partialResults = new List<String>();

            using (var reader = new WaveFileReader(path))
            {

                if (!ProperFormat(reader.WaveFormat))
                {
                    Log.Info($"Wrong format - Cannot extract data from file {path}");
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

            return results.Any() ? results : partialResults;
        }

        private bool ProperFormat(WaveFormat waveFormat)
        {
            return waveFormat.SampleRate >= FormatRequirements.MinSamplingRate
                & waveFormat.Channels == FormatRequirements.MaxChannelsNumber
                & waveFormat.BitsPerSample == FormatRequirements.BitsPerSample
                & waveFormat.Encoding.Equals(FormatRequirements.Encoding);
        }

        private void AddResult(string fullResult, List<string> results, string property)
        {
            using var doc = JsonDocument.Parse(fullResult);
            string res = doc.RootElement.GetProperty(property).GetString();

            if (res != "")
                results.Add(res);
        }
    }
}
