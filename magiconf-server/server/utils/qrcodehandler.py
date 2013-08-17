'''
Created on 8 de Jun de 2013

@author: David
'''


from PyQRNative import *

class QRCodeHandler():
    
    qr= None
    im = None
    data = ""
    QRCODE_MATRIX_SIZE=10 
    
    def __init__(self,data):
        self.data = data
        
    def buildQRCode(self):
        self.qr = QRCode(self.QRCODE_MATRIX_SIZE, QRErrorCorrectLevel.L)
        self.qr.addData(self.data)
        self.qr.make()
        self.im = self.qr.makeImage()
        
    
    def saveImageOnDisk(self,path):
        self.im.save(path)
    
    def getImage(self):
        return self.im
