import wx
import FreqData
from pubsub import pub
import FreqCurveTab
from FreqStatus import FreqState, FreqStatistics, FreqStatus


class ActFreqPanel(wx.Panel):
    minFreq = 100.00
    maxFreq = 0.00

    def __init__(self, parent):
        wx.Panel.__init__(self, parent, size = (200, 50))
        self.sBox = wx.StaticBox(self, label='Aktuelle Frequenz')
        self.Bind(wx.EVT_SIZE, self.OnResize)
        hbox = wx.BoxSizer(wx.HORIZONTAL)
        vbox = wx.BoxSizer(wx.VERTICAL)

        # I want this line visible in the CENTRE of the inner panel
        font = wx.Font(13, wx.DEFAULT, wx.BOLD, wx.DEFAULT)
        self.freqText = wx.StaticText(self, id=-1, label="??.??", style=wx.ALIGN_CENTER, name="")
        self.freqText.SetFont(font)
        hbox.Add(self.freqText, 0, wx.ALL|wx.ALIGN_CENTER)
        vbox.Add(hbox, 1, wx.ALL|wx.ALIGN_CENTER, 5)

        hboxMinMax = wx.BoxSizer(wx.HORIZONTAL)
        fontMinMax = wx.Font(10, wx.DEFAULT, wx.NORMAL, wx.DEFAULT)
        self.freqMinText = wx.StaticText(self, id=-1, label="min: ??.??", style=wx.ALIGN_CENTER, name="")
        self.freqMinText.SetFont(fontMinMax)
        hboxMinMax.Add(self.freqMinText, 0, wx.LEFT|wx.ALIGN_LEFT, 5)
        self.freqMaxText = wx.StaticText(self, id=-1, label="max: ??.??", style=wx.ALIGN_RIGHT, name="")
        self.freqMaxText.SetFont(fontMinMax)

        hboxMinMax.Add((150, -1), flag=wx.EXPAND)
        hboxMinMax.Add(self.freqMaxText, proportion=0, flag=wx.RIGHT|wx.ALIGN_CENTER)

        vbox.Add(hboxMinMax, proportion=0, flag=wx.ALL|wx.ALIGN_LEFT, border=5)

        self.SetSizer(vbox)
        vbox.Fit(self)

    def setFreq(self, freq):
        try:
            dFreq = float(freq)
            if dFreq > self.maxFreq:
                self.maxFreq = dFreq
                sFreq = str(self.maxFreq)[:6]
                self.freqMaxText.SetLabel('max: ' + sFreq)
            if dFreq < self.minFreq:
                self.minFreq = dFreq
                self.freqMinText.SetLabel('min: ' + str(self.minFreq)[:6])
        except ValueError:
            pass
        self.freqText.SetLabel(freq[:6])
        self.freqText.SetLabel(freq[:6])

    def OnResize(self, event):
        event.Skip()

        self.sBox.SetSize(event.Size)
        self.Layout()


class ActStatusPanel(wx.Panel):
    def __init__(self, parent):
        wx.Panel.__init__(self, parent, size = (200,50))
        self.sBox = wx.StaticBox(self, label='Aktueller Status')
        self.Bind(wx.EVT_SIZE, self.OnResize)

        hbox = wx.BoxSizer(wx.HORIZONTAL)
        vbox = wx.BoxSizer(wx.VERTICAL)

        # I want this line visible in the CENTRE of the inner panel
        font = wx.Font(12, wx.DEFAULT, wx.NORMAL, wx.DEFAULT)
        self.statText = wx.StaticText(self, id=-1, style=wx.ALIGN_CENTER, name="")
        self.statText.SetFont(font)
        self.setState(FreqState.NoNet)

        hbox.Add(self.statText, 0, wx.ALL|wx.ALIGN_CENTER)
        vbox.Add(hbox, 1, wx.ALL|wx.ALIGN_CENTER, 5)

        self.SetSizer(vbox)
        vbox.Fit(self)

    def setState(self, state):
        if state == FreqState.Ok:
            self.statText.SetLabel("OK")
        elif state == FreqState.Warning:
            self.statText.SetLabel("WARNUNG: Störung")
        elif state == FreqState.Error:
            self.statText.SetLabel("FEHLER: Netz ausserhalb zulässiger Regeln")
        elif state == FreqState.Blackout:
            self.statText.SetLabel("BLACKOUT")
        elif state == FreqState.NoNet:
            self.statText.SetLabel("KEIN NETZ")

    def OnResize(self, event):
        event.Skip()

        self.sBox.SetSize(event.Size)
        self.Layout()

class FreqStatisticsPanel(wx.Panel):
    freqStats = FreqStatistics()
    def __init__(self, parent):
        wx.Panel.__init__(self, parent, size = (200,110))
        vbox = wx.StaticBoxSizer(wx.VERTICAL, self, label='Frequenz Statistik')
        vbox.Add(10,5,0)  # Border oben
        # I want this line visible in the CENTRE of the inner panel
        font = wx.Font(11, wx.DEFAULT, wx.NORMAL, wx.DEFAULT)
        self.cntWarnTxt = wx.StaticText(self, id=-1, style=wx.ALIGN_LEFT, name="")
        self.cntWarnTxt.SetFont(font)
        vbox.Add(self.cntWarnTxt, 0, wx.ALIGN_LEFT, 5)
        self.cntErrTxt = wx.StaticText(self, id=-1, style=wx.ALIGN_LEFT, name="")
        self.cntErrTxt.SetFont(font)
        vbox.Add(self.cntErrTxt, 0, wx.ALIGN_LEFT, 0)
        self.cntBlackoutTxt = wx.StaticText(self, id=-1, style=wx.ALIGN_LEFT, name="")
        self.cntBlackoutTxt.SetFont(font)
        vbox.Add(self.cntBlackoutTxt, 0, wx.ALIGN_LEFT, 0)
        self.cntNetTxt = wx.StaticText(self, id=-1, style=wx.ALIGN_LEFT, name="")
        self.cntNetTxt.SetFont(font)
        vbox.Add(self.cntNetTxt, 0, wx.ALIGN_LEFT, 0)

        self.SetSizer(vbox)
        vbox.Fit(self)

        self.update()

    def setStats(self, freqStats):
        self.freqStats = freqStats
        self.update()

    def update(self):
        self.cntWarnTxt.SetLabel(    'Anzahl Warnungen:        %d' % self.freqStats.countWarning)
        self.cntErrTxt.SetLabel(     'Anzahl Fehler:                %d' % self.freqStats.countError)
        self.cntBlackoutTxt.SetLabel('Anzahl Blackouts:           %d' % self.freqStats.countBlackout)
        self.cntNetTxt.SetLabel(     'Anzahl Netzwerkfehler:  %d' % self.freqStats.countNoNet)

class FreqStatsTab(wx.Panel):
    def __init__(self, parent):
        """"""
        wx.Panel.__init__(self, parent=parent)

        topsizer = wx.BoxSizer(wx.VERTICAL)

        self.actFreqPnl = ActFreqPanel(self)
        topsizer.Add(
                self.actFreqPnl,
                1,           # make vertically stretchable
                wx.EXPAND |  # make horizontally stretchable
                wx.ALL,      # and make border all around
                10)          # set border width to 10

        self.actStatusPnl = ActStatusPanel(self)
        topsizer.Add(
                self.actStatusPnl,
                1,           # make vertically stretchable
                wx.EXPAND |  # make horizontally stretchable
                wx.ALL,      # and make border all around
                10)          # set border width to 10

        self.freqStatsPnl = FreqStatisticsPanel(self)
        topsizer.Add(
                self.freqStatsPnl,
                1,           # make vertically stretchable
                wx.EXPAND |  # make horizontally stretchable
                wx.ALL,      # and make border all around
                10)          # set border width to 10

        self.SetSizerAndFit(topsizer) # use the sizer for layout and size window
                                      # accordingly and prevent it from being resized
                                      # to smaller size

        self.Show()

    def update(self, message, arg2=None):
#        print(f"Received the following message: {message}")
        freq = self.freqStatus.setValue(message)
        #print(self.freqStatus.getValue())
        self.actFreqPnl.setFreq(freq)
        self.actStatusPnl.setState(self.freqStatus.getStatus())
        self.freqStatsPnl.setStats(self.freqStatus.getStatistics())

    def OnClose(self, event):
        self.thread.stop()
        self.Destroy()  # you may also do:  event.Skip()
                        # since the default event handler does call Destroy(), too

    def setFreq(self, freq):
        self.actFreqPnl.setFreq(freq)

    def setState(self, status):
        self.actStatusPnl.setState(status)

    def setStats(self, stats):
        self.freqStatsPnl.setStats(stats)

class GridFrame(wx.Frame):
    def __init__(self, parent):
        wx.Frame.__init__(self, parent, title="Netzfrequenz Überwachung", size=(400,500))

        panel = wx.Panel(self)

        notebook = wx.Notebook(panel)
        self.actStatusPnl = FreqStatsTab(notebook)
        notebook.AddPage(self.actStatusPnl, "Frequenz Status")

        self.freqCurvePnl = FreqCurveTab.FreqCurveTab(notebook)
        notebook.AddPage(self.freqCurvePnl, "Frequenz Kurve")

        sizer = wx.BoxSizer(wx.VERTICAL)
        sizer.Add(notebook, 1, wx.ALL | wx.EXPAND, 5)
        panel.SetSizer(sizer)
        self.Layout()

        self.Show()

        self.freqStatus = FreqStatus()
        self.Bind(wx.EVT_CLOSE, self.OnClose)

        pub.subscribe(self.update, "freq_listener")
        self.thread = FreqData.FreqDataThread()

    def update(self, message, arg2=None):
#        print(f"Received the following message: {message}")
        freq = self.freqStatus.setValue(message)
        #print(self.freqStatus.getValue())
        self.actStatusPnl.setFreq(freq)
        self.actStatusPnl.setState(self.freqStatus.getStatus())
        self.actStatusPnl.setStats(self.freqStatus.getStatistics())
        self.freqCurvePnl.setFreq(freq)

    def OnClose(self, event):
        self.thread.stop()
        self.Destroy()  # you may also do:  event.Skip()
                        # since the default event handler does call Destroy(), too

if __name__ == '__main__':

    app = wx.App(0)
    frame = GridFrame(None)
    app.MainLoop()