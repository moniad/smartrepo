using log4net;
using NAudio.Wave;
using System;
using System.Collections.Generic;
using System.IO;
using Optional;
using Vosk;

namespace VoskAudioParser
{
    class AudioParser
    {
        private static readonly ILog log = LogManager.GetLogger(typeof(AudioParser));

        private readonly TextExtractor extractor = new();

        public List<string> ParseWaveFile(String path, Model model)
        {
            var tmpFile = Option.None<string>();

            using (var reader = new WaveFileReader(path))
            {

                int sampleRate = reader.WaveFormat.SampleRate;
                int channelsNumer = reader.WaveFormat.Channels;

                if (sampleRate < FormatRequirements.minSamplingRate || channelsNumer > FormatRequirements.maxChannelsNumber)
                {
                    var outFile = CreateTempFile(path);
                    var outFormat = new WaveFormat(Math.Max(sampleRate, FormatRequirements.minSamplingRate),
                        FormatRequirements.bitsPerSample,
                        Math.Min(channelsNumer, FormatRequirements.maxChannelsNumber));
                    using var resampler = new MediaFoundationResampler(reader, outFormat);
                    WaveFileWriter.CreateWaveFile(outFile, resampler);

                    path = outFile;
                    tmpFile = Option.Some(outFile);
                }
            }

            var results = extractor.ExtractFromWaveFile(path, model);

            foreach (var filePath in tmpFile)
            {
                DeleteFile(filePath);
            }

            return results;
        }

        public List<string> ParseAudioFile(String path, Model model)
        {

            string outFile;

            using (var reader = new MediaFoundationReader(path))
            {
                int sampleRate = reader.WaveFormat.SampleRate;
                int channelsNumer = reader.WaveFormat.Channels;

                outFile = CreateTempFile(path);
                var outFormat = new WaveFormat(Math.Max(sampleRate, FormatRequirements.minSamplingRate),
                    FormatRequirements.bitsPerSample,
                    Math.Min(channelsNumer, FormatRequirements.maxChannelsNumber));
                using var resampler = new MediaFoundationResampler(reader, outFormat);
                WaveFileWriter.CreateWaveFile(outFile, reader);
            }

            var results = extractor.ExtractFromWaveFile(outFile, model);

            DeleteFile(outFile);

            return results;
        }

        private string CreateTempFile(String path)
        {
            var fileName = Path.GetFileName(path);
            var tmpDir = @"tmp\";
            Directory.CreateDirectory(tmpDir);
            Console.WriteLine(Path.Combine(tmpDir, fileName));
            return Path.Combine(tmpDir, fileName);
        }

        private static void DeleteFile(String path)
        {
            if (File.Exists(path))
            {
                try
                {
                    File.Delete(path);
                }
                catch (IOException e)
                {
                    log.Error(e.Message);
                }
            }
            else
            {
                log.Debug($"Temporary file {path} does not exist");
            }
        }

    }
}
