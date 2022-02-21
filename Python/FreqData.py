
import time
import requests
import re
from threading import Thread
import wx
from pubsub import pub

class FreqData:
    def get(self):
        try:
            r = requests.get('https://www.netzfrequenz.info/verlauf-3-minuten-tennet50hertz/../json/lsdtest.php'
                             , timeout=6000)
        except:
            return [""]
        if r.ok == True:
            p = re.compile(r'\d+')
            timestamp = p.findall(r.text)[0]
            #print(timestamp)
            p = re.compile('[0-9]*[.][0-9]+')
            freq = p.findall(r.text)
            # print(freq)
            return freq
        else:
            return [""]


class FreqDataThread(Thread):

    def __init__(self):
        """Init Worker Thread Class."""
        self.freqData = FreqData()
        self.data = ['??.??', '??.??']
        self.running = True
        Thread.__init__(self)
        self.start()  # start the thread

    def run(self):
        """Run Worker Thread."""
        while (self.running):
            time.sleep(1)
            self.data = self.freqData.get()
            wx.CallAfter(self.postData, 0)

    def stop(self):
        self.running = False

    def postData(self, amt):
        """
        Send data to GUI
        """
        pub.sendMessage("freq_listener", message=self.data)
