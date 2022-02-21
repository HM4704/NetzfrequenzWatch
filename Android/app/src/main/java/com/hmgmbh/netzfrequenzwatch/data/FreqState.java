package com.hmgmbh.netzfrequenzwatch.data;

public enum FreqState {
    Ok,                  // Bereich 49.81 - 50.19                   :  keine Störung
    Warning,             // Bereiche 49.80 - 49.49, 50.20 - 50.49   :  gestörter Betrieb
    Error,               // Bereiche <=49.50, >= 50.50              :  Betrieb ausserhalb Spezifikation
    Blackout,            // Bereiche <= 47,50, >= 51,50
    NoNet                // keine Verbindung zum Server
}
