import keyboard
import pyvjoy
from time import sleep

# command:
# python -m PyInstaller --onefile --name "run me" --uac-admin --hidden-import=keyboard src\main\java\frc\robot\util\keyboardRead.py
# import pyautogui

# Connect to vJoy Device 1
j = pyvjoy.VJoyDevice(1)

ARROW_SCAN_CODES = {72, 80, 75, 77}  # up, down, left, right


def addButton(index: int, key: str):
    def key_handler(e):
        if e.name == key:
            state = 1 if e.event_type == "down" else 0
            j.set_button(index, state)
            print(key)

    keyboard.hook(key_handler)


#  generic button function. change as you see fit
addButton(1, "b")


keyboard.wait()
