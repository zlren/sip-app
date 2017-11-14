package com.zczg.util;

//import java.io.FileOutputStream;
//import com.voicerss.tts.AudioCodec;
//import com.voicerss.tts.AudioFormat;
//import com.voicerss.tts.Languages;
//import com.voicerss.tts.SpeechDataEvent;
//import com.voicerss.tts.SpeechDataEventListener;
//import com.voicerss.tts.SpeechErrorEvent;
//import com.voicerss.tts.SpeechErrorEventListener;
//import com.voicerss.tts.VoiceParameters;
//import com.voicerss.tts.VoiceProvider;

public class VoiceConf {
//	private static String KEY = "f0cd18f621f64f60a872d7ad5e6d6d7f";
//	private static String PATH = "";
//	
//	public static void createVoice(String name, String sentence) throws Exception{
//		VoiceProvider tts = new VoiceProvider(KEY);
//		
//        VoiceParameters params = new VoiceParameters(sentence, Languages.English_UnitedStates);
//        params.setCodec(AudioCodec.WAV);
//        params.setFormat(AudioFormat.Format_8KHZ.AF_8khz_16bit_mono);
//        params.setBase64(false);
//        params.setSSML(false);
//        params.setRate(0);
//		
//        byte[] voice = tts.speech(params);
//		
//        FileOutputStream fos = new FileOutputStream(PATH + name + ".wav");
//        fos.write(voice, 0, voice.length);
//        fos.flush();
//        fos.close();
//	}
//	
//    public static void main (String args[]) throws Exception {
//        createVoice("force_prompt", "The user you are dialing is under calling. Press 1 to force break. Press 2 to force insert.");
//        System.out.println("finish");
//    }
}