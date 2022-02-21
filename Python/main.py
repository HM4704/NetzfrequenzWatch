import tkinter as tk
#import serial
import datetime
import FreqData
import requests
import re

step_x = 2

freq = FreqData.FreqData()


def getFreq():
    r = requests.get('https://www.netzfrequenz.info/verlauf-3-minuten-tennet50hertz/../json/lsdtest.php')
    p = re.compile(r'\d+')
    timestamp = p.findall(r.text)[0]
    print(timestamp)
    p = re.compile('[0-9]*[.][0-9]+')
    freq = p.findall(r.text)
    #print(freq)
    return freq


def scope(cv, x, step_x, pt):

    def measure_point():
        try:
            r = freq.get() #getFreq()
            v = float(r[0])
            u = (125030.0 / (v/50))
            #u = 50 #int(ser.readline())             # u = AtTiny-Zaehlerschritte
            #v = (125030.0 / u)                  # normierter Kehrwert
            #v = v * 50                          # Frequenz in Hz
            print ("{0:2.3f}".format(v))
            u = 260 + (u - 125030.0)            # zufaellig kann "u" ohne Multiplikator verwendet werden
            if u < 0 or u > 520:                # bei Fehlmessungen auf Mittellinie setzen
                u = 260
                print("Fehler")
        except:
            u = 260
            print("Fehler")
        return u

    if x < 720:
        if pt > 0:
            last_y = cv.coords(pt)[-1]
        else:
            cv.delete("line_point")
            last_y = 250

        x = x + step_x
        pt = cv.create_line(x-step_x, last_y , x, measure_point(), \
                            fill = "blue", tag="line_point", width=2)
    else:
        x = 0
        pt = 0
    cv.after(1000, scope, cv, x, step_x, pt)

root = tk.Tk()

root.title("Zeitlicher Verlauf der Netzfrequenz")
cv = tk.Canvas(root, width=730, height=550, bg="white")
cv.pack(padx=5, pady=5)
for n in range (0,50):
    cv.create_line(0, 510-n*25, 720, 510-n*25, fill = "lightblue") # waagrechte Rasterlinien
for n in range (1,14):
    cv.create_line(n*60, 10, n*60, 510, fill = "lightblue") # senkrechte Rasterlinien
for n in range (1,20):
    cv.create_text(0, 510-n*50, text=str(49.9 + n*0.02) , anchor="w") # senkrechte Beschirftung
for n in range (1,8):
    cv.create_text(n*120, 530, text=str(n*60), anchor="s") # waagrechte Beschriftung
cv.create_line(0, 235, 720, 235, fill = "black")  # schwarze Linie Beginn Regelbereich
cv.create_line(0, 285, 720, 285, fill = "black")  # schwarze Linie Beginn Regelbereich

tk.Label(root, text="senkrecht: Netzfrequenz in Hz         waagrecht: Sekunden").pack()
scope(cv, 0, step_x, 0)
tk.Button(root, text="Speichern", command=lambda :  cv.postscript(
    file="cv.{0:%d%b%Y_%H_%M_%S.%f}.ps".format(
    datetime.datetime.utcnow()))).pack()

#ser = serial.Serial('/dev/ttyUSB1', 115200, dsrdtr=1, rtscts=1)
# dsrdtr=1 und rtscts=1: Spannungsversorgung fuer LP Mikrocontroller-Board
#ser.flush()

tk.mainloop()