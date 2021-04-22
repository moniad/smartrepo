using NAudio.Wave;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace VoskAudioParser
{
    public static class FormatRequirements
    {
        public static int MinSamplingRate = 16000;

        public static int MaxChannelsNumber = 1;

        public static int BitsPerSample = 16;

        public static WaveFormatEncoding Encoding = WaveFormatEncoding.Pcm;
    }
}
