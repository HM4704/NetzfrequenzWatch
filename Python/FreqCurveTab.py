import matplotlib
matplotlib.use('WXAgg')
matplotlib.interactive(True)
from matplotlib.backends.backend_wxagg import FigureCanvasWxAgg as FigureCanvas
#from matplotlib.backends.backend_wx import NavigationToolbar2Wx
from matplotlib.figure import Figure

import wx
import numpy as np
import collections

class FreqCurveTab(wx.Panel):
    def __init__(self, parent):
        wx.Panel.__init__(self, parent)
        self.figure = Figure()
        self.axes = self.figure.add_subplot(111)
        self.canvas = FigureCanvas(self, -1, self.figure)
        self.sizer = wx.BoxSizer(wx.VERTICAL)
        self.sizer.Add(self.canvas, 1, wx.LEFT | wx.TOP | wx.GROW)
        self.SetSizer(self.sizer)
        self.Fit()

        self.Bind(wx.EVT_PAINT, self.OnPaint)

        self.freq = collections.deque(np.zeros(1))
        self.draw()

    def setFreq(self, freq):
        try:
            self.freq.append(float(freq))
            self.canvas.draw()
            self.Refresh()
        except:
            pass

    def draw(self):
        self.axes.clear()
        self.axes.set_ylim(49.6, 50.4)
        self.axes.plot(self.freq)

    def OnPaint(self, event):
        self.draw()
