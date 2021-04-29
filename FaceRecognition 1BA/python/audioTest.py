from pygame import mixer

mixer.init()

sound = mixer.Sound('audio/Escalon.wav')

sound.set_volume(0.03)

sound.play()