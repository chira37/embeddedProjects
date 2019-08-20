from gps import * # import gps libarary
import time, inspect 
import os # to run terminal command
from firebase import firebase # firebase library

from RPLCD import CharLCD
lcd = CharLCD(cols=16, rows=2, pin_rs=37, pin_e=35, pins_data=[33, 31, 29, 23])
lcd.write_string(u'Intializing')


os.system('sudo gpsd /dev/ttyS0 -F /var/run/gpsd.sock') # termian command to connnect gpsd to serail port


firebase = firebase.FirebaseApplication('https://bustracker-c4a91.firebaseio.com') # firebase databse address

gpsd = gps(mode=WATCH_ENABLE|WATCH_NEWSTYLE)

time.sleep(10)

try:
    
    while True:
        
        report = gpsd.next()
        
        if report['class'] == 'TPV':
          
            lat = getattr(report,'lat',0.0)
            
            if( lat == 0):
                lcd.clear()
                lcd.write_string(u'No GPS signal')
            
            else:
                lcd.clear()
                lcd.write_string(u'GPS is connected')
                
            lon = getattr(report,'lon',0.0)
            speed =  getattr(report,'speed',0.0)
            
            firebase.put('users/bus1',"lat",lat)
            firebase.put('users/bus1',"lon",lon)
            firebase.put('users/bus1',"speed",speed)
            
            print  lat,"\t",
            print  lon,"\t",
            print  speed,"\t"
            
 
            time.sleep(10)
 
except(KeyboardInterrupt, SystemExit):
    print "Done.\nExiting."
    f.close()

