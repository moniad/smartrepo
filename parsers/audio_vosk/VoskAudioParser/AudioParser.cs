using log4net;
using NAudio.Wave;
using System;
using System.IO;
using Vosk;
using NAudio.Wave.SampleProviders;

namespace VoskAudioParser
{
    public class AudioParser
    {
        private static readonly ILog Log = LogManager.GetLogger(typeof(AudioParser));

        private readonly TextExtractor Extractor = new();

        public string[] AcceptedExtensions { get; } = { "wav" };

        public string ParseWaveFile(String path, Model model)
        {
            var tmpFile = CreateTempFile(path);
            if (Resample(path, tmpFile))
            {
                path = tmpFile;
            }
            if (StereoToMono(path, tmpFile))
            {
                path = tmpFile;
            }

            var results = Extractor.ExtractFromWaveFile(path, model);

            DeleteFile(tmpFile);

            return String.Join("\n", results); ;
        }

        private bool Resample(string path, string outFile)
        {
            using (var reader = new AudioFileReader(path))
            {
                int sampleRate = reader.WaveFormat.SampleRate;

                if (sampleRate < FormatRequirements.MinSamplingRate)
                {
                    var resampler = new WdlResamplingSampleProvider(reader, Math.Max(sampleRate, FormatRequirements.MinSamplingRate));
                    WaveFileWriter.CreateWaveFile16(outFile, resampler);

                    return true;
                }
            }
            return false;
        }

        private bool StereoToMono(string path, string outFile)
        {
            using (var reader = new AudioFileReader(path))
            {
                int channelsNumber = reader.WaveFormat.Channels;

                if (channelsNumber > FormatRequirements.MaxChannelsNumber)
                {
                    var sampleProvider = new StereoToMonoSampleProvider(reader)
                    {
                        LeftVolume = 0.0f,
                        RightVolume = 1.0f
                    };
                    WaveFileWriter.CreateWaveFile16(outFile, sampleProvider);

                    return true;
                }
            }
            return false;
        }

        private string CreateTempFile(String path)
        {
            var fileName = Path.GetFileNameWithoutExtension(path) + ".wav";
            var tmpDir = @"tmp";
            Directory.CreateDirectory(tmpDir);
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
        }

    }
}
