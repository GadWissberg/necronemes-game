package com.gadarts.necronemes;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.gadarts.necromine.assets.Assets;
import com.gadarts.necromine.assets.GameAssetsManager;
import com.gadarts.necronemes.utils.GeneralUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import static com.badlogic.gdx.math.MathUtils.random;
import static com.badlogic.gdx.math.MathUtils.randomBoolean;

public class SoundPlayer {
	public static final boolean SFX_ENABLED = false;
	private static final float MELODY_VOLUME = 0.4f;
	private static final float PITCH_OFFSET = 0.1f;
	private final GameAssetsManager assetManager;
	private final List<Sound> loopingSounds = new ArrayList<>();
	@Getter
	@Setter
	private boolean sfxEnabled;
	@Getter
	private boolean musicEnabled;

	public SoundPlayer(final GameAssetsManager assetManager) {
		this.assetManager = assetManager;
		setSfxEnabled(DefaultGameSettings.SFX_ENABLED);
		setMusicEnabled(DefaultGameSettings.MELODY_ENABLED);
	}
	

	public void setMusicEnabled(final boolean musicEnabled) {
		this.musicEnabled = musicEnabled;
		if (musicEnabled) {
			playMusic(Assets.Melody.TEST);
		} else {
			stopMusic(Assets.Melody.TEST);
		}
	}

	public void playMusic(final Assets.Melody melody) {
		if (!isMusicEnabled()) return;
		Music music = assetManager.getMelody(melody);
		music.setVolume(MELODY_VOLUME);
		music.setLooping(true);
		music.play();
	}

	public void stopMusic(final Assets.Melody melody) {
		Music music = assetManager.getMelody(melody);
		music.stop();
	}

	public void playSound(final Assets.Sounds soundDef) {
		playSound(soundDef, 1F);
	}

	public void playSound(final Assets.Sounds def, final float volume) {
		if (!isSfxEnabled()) return;
		float pitch = 1 + (def.isRandomPitch() ? (randomBoolean() ? 1 : -1) : 0) * random(-PITCH_OFFSET, PITCH_OFFSET);
		if (!def.isLoop()) {
			assetManager.getSound(getRandomSound(def)).play(volume, pitch, 0);
		} else {
			Sound sound = assetManager.getSound(getRandomSound(def));
			sound.loop(volume, 1, 0);
			loopingSounds.add(sound);
		}
	}

	private String getRandomSound(final Assets.Sounds soundDef) {
		String filePath = soundDef.getFilePath();
		if (soundDef.getFiles().length > 0) {
			filePath = GeneralUtils.getRandomRoadSound(soundDef);
		}
		return filePath;
	}

	public void stopLoopingSounds( ) {
		loopingSounds.forEach(Sound::stop);
	}
}
