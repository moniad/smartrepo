using log4net;
using NAudio.Wave;
using System;
using System.Collections.Generic;
using System.IO;
using Optional;
using Vosk;
using NAudio.Wave.SampleProviders;
using NLayer.NAudioSupport;

namespace VoskAudioParser
{
    public class AudioParser
    {
        private static readonly ILog Log = LogManager.GetLogger(typeof(AudioParser));

        private readonly TextExtractor Extractor = new();

        public string[] AcceptedExtensions { get; } = { "wav", "mp3" };

        public string Parse(String path, Model model, String extension)
        {
            var results = String.Equals(extension, "wav") ? ParseWaveFile(path, model) : ParseMp3File(path, model);
            Log.Info(results);
            return String.Join("\n", results);
        }

        public List<string> ParseMp3File(String path, Model model)
        {
            string outFile;

            using (var reader = new Mp3FileReader(path, wf => new Mp3FrameDecompressor(wf)))
            {
                outFile = CreateTempFile(path);
                WaveFileWriter.CreateWaveFile(outFile, reader);
            }

            var results = ParseWaveFile(path, model);

            DeleteFile(outFile);

            return results;
        }

        public List<string> ParseWaveFile(String path, Model model)
        {
            var tmpFile = Resample(path);
            tmpFile = StereoToMono(tmpFile.ValueOr(path));

            var results = Extractor.ExtractFromWaveFile(tmpFile.ValueOr(path), model);

            foreach (var filePath in tmpFile)
            {
                DeleteFile(filePath);
            }

            return results;
        }

        private Option<string> Resample(string path)
        {
            using (var reader = new AudioFileReader(path))
            {
                int sampleRate = reader.WaveFormat.SampleRate;

                if (sampleRate < FormatRequirements.MinSamplingRate)
                {
                    var outFile = CreateTempFile(path);
                    var resampler = new WdlResamplingSampleProvider(reader, Math.Max(sampleRate, FormatRequirements.MinSamplingRate));
                    WaveFileWriter.CreateWaveFile16(outFile, resampler);

                    return Option.Some(outFile);
                }
            }
            return Option.None<string>();
        }

        private Option<string> StereoToMono(string path)
        {
            using (var reader = new AudioFileReader(path))
            {
                int channelsNumer = reader.WaveFormat.Channels;

                if (channelsNumer > FormatRequirements.MaxChannelsNumber)
                {
                    var outFile = CreateTempFile(path);
                    var sampleProvider = new StereoToMonoSampleProvider(reader)
                    {
                        LeftVolume = 0.0f,
                        RightVolume = 1.0f
                    };
                    WaveFileWriter.CreateWaveFile16(outFile, sampleProvider);

                    return Option.Some(outFile);
                }
            }
            return Option.None<string>();
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
                    Log.Error(e.Message);
                }
            }
            else
            {
                Log.Debug($"Temporary file {path} does not exist");
            }
        }

    }
}
