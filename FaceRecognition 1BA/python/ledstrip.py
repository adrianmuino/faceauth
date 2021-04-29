import time
from rpi_ws281x import *
import RPi.GPIO as GPIO
from pygame import mixer

GPIO.setwarnings(False)
GPIO.setmode(GPIO.BCM)
GPIO.setup(20, GPIO.IN, pull_up_down=GPIO.PUD_UP)
GPIO.setup(16, GPIO.IN, pull_up_down=GPIO.PUD_UP)
GPIO.setup(19, GPIO.IN, pull_up_down=GPIO.PUD_UP)

led_count = 300      # Number of LED pixels.
GPIO_PIN= 21      # GPIO pin connected to the pixels (18 uses PWM!).
frequency = 800000  # This is the recommended frequency according to Adafruit
led_dma = 10      # DMA channel to use for generating signal
brightness = 255 	
led_invert = False   # Without this all lights would go crazy
led_channel = 0   	# usually set to 0
mixer.init()

def turnoff(strip, color):
    for i in range(0, led_count):
        strip.setPixelColor(i, color)
    strip.show() 

def happy(strip, color):
    for i in range(0, led_count):
        strip.setPixelColor(i, color)
    strip.show()

def sad(strip, color):
    for i in range(0, led_count):
        strip.setPixelColor(i, color)
    strip.show()

def neutral(strip, color):
    for i in range(0 , led_count):
        strip.setPixelColor(i, color)
    strip.show()

def playSong(song):
    sound = mixer.Sound(song)
    sound.set_volume(0.03)
    sound.play()

strip = Adafruit_NeoPixel(led_count, GPIO_PIN, frequency, led_dma, led_invert, brightness, led_channel)
strip.begin()

turnoff(strip, Color(0,0,0))

while True:
    if GPIO.input(20) == False:
        happy(strip, Color(186, 85, 211)) #Purple’s effects are directed at the creative mind, this hue will help harness your creative energies.
        playSong('audio/Happy.wav')
    elif GPIO.input(16) == False:
        sad(strip, Color(255, 165, 0)) #Orange is particularly great at adding a bit of optimism to your day.
        playSong('audio/Sad.wav')
    elif GPIO.input(19) == False:
        neutral(strip, Color(144, 238, 144)) # Green is especially easy on the eyes, it’s ideal for creating harmony and peace.
        playSong('audio/Escalon.wav')