using NAudio.Wave;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace VoskAudioParser
{
    static class FormatRequirements
    {

        public static int minSamplingRate = 16000;

        public static int maxChannelsNumber = 1;

        public static int bitsPerSample = 16;

        public static WaveFormatEncoding encoding = WaveFormatEncoding.Pcm;

    }
}
