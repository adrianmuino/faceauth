import RPi.GPIO as GPIO

import time

import sys



GPIO.setwarnings(False)

GPIO.setmode(GPIO.BCM)

GPIO.setup(21, GPIO.OUT)



P = GPIO.PWM(21, 50)

P.start(0)



#answer = raw_input("Choose what to do: ")



def unlock():

    P.ChangeDutyCycle(7.5)

    print("Your door is unlocked")

    time.sleep(1)



def lock():

    P.ChangeDutyCycle(2.5)

    print("Your door is locked")

    time.sleep(1)

    

if sys.argv[1] == "unlock":

    unlock()

    time.sleep(10)

    lock()



elif sys.argv[1] == "lock":

    lock()



else:

    lock()

    print("You do not have access to this house")

    

P.stop()

GPIO.cleanup()
