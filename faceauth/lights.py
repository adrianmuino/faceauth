import time

from rpi_ws281x import *

import sys



led_count = 300 # Number of LED pixels.

GPIO_PIN= 21 # GPIO pin connected to the pixels (18 uses PWM!).

frequency = 800000 # This is the recommended frequency according to Adafruit

led_dma = 10 # DMA channel to use for generating signal

brightness = 255

led_invert = False # Without this all lights would go crazy

led_channel = 0 # usually set to 0



def happy(strip, color):

    for i in range(0, led_count):

        strip.setPixelColor(i, color)

        strip.show()

        time.sleep(0.025)



def sad(strip, color):

    for i in range(0, led_count):

        strip.setPixelColor(i, color)

    strip.show()



def turnoff(strip, color):

    for i in range(0, led_count):

        strip.setPixelColor(i, color)

    strip.show()

    

strip = Adafruit_NeoPixel(led_count, GPIO_PIN, frequency, led_dma, led_invert, brightness, led_channel)

strip.begin()



turnoff(strip, Color(0,0,0))

####x = input("Are you sad? [Yes/No] ")



if sys.argv[1] == 'sad':

    print("I hope you enjoy this mood base lighting")

    time.sleep(3)

    sad(strip, Color(150, 150, 150))

elif sys.argv[1] == 'happy':

    print ("Enjoy the lights!!!")

    happy(strip, Color(255, 0, 0))

else:

    turnoff(strip, Color(0,0,0))
