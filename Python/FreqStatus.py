from dataclasses import dataclass
from enum import Enum

import FreqData


class FreqState(Enum):
    Ok = 0                  # Bereich 49.81 - 50.19                   :  keine Störung
    Warning = 1             # Bereiche 49.80 - 49.49, 50.20 - 50.49   :  gestörter Betrieb
    Error = 2               # Bereiche <=49.50, >= 50.50              :  Betrieb ausserhalb Spezifikation
    Blackout = 3            # Bereiche <= 47,50, >= 51,50
    NoNet = 4               # keine Verbindung zum Server


@dataclass
class FreqStatistics:
    countWarning: int = 0
    countError: int = 0
    countBlackout: int = 0
    countNoNet: int = 0


class FreqStatus():
    freqStats = FreqStatistics()
    def __init__(self):
        self.data = FreqData.FreqData()
        self.status = FreqState.NoNet

    def getValue(self):
        values = self.data.get()
        return self.determineState(values)

    def setValue(self, values):
        return self.determineState(values)

    def getStatus(self):
        return self.status

    def getStatistics(self):
        return self.freqStats

    def determineState(self, values):
        if len(values) == 2:
            self.status = self.evaluate(values[0])
            return values[0]
        else:
            self.status = FreqState.NoNet
            self.freqStats.countNoNet = self.freqStats.countNoNet + 1
            return "??.??"


    def evaluate(self, value):
        value = float(value)
        if value <= 47.50 or value >= 51.50:
            self.freqStats.countBlackout = self.freqStats.countBlackout + 1
            return FreqState.Blackout
        if value <= 49.50 or value >= 50.50:
            self.freqStats.countError = self.freqStats.countError + 1
            return FreqState.Error
        if 49.80 < value < 50.20:
            return FreqState.Ok
        self.freqStats.countWarning = self.freqStats.countWarning + 1
        return FreqState.Warning