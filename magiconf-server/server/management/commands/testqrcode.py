from django.core.management.base import BaseCommand, CommandError
from server.SiCientificoApi import SICientificoAPI
from optparse import make_option
from encodings import ascii
from string import ascii_letters

from PyQRNative import *
from server.utils.qrcodehandler import QRCodeHandler


class Command(BaseCommand):
    args = '<poll_id poll_id ...>'
    help = 'qrcode tester'
    option_list = BaseCommand.option_list + (
            make_option('--all','-a', dest='update',
            default=False,
            help='qrcode tester'),
        )


    def handle(self, *args, **options):
        qr = QRCodeHandler("sapo")
        qr.buildQRCode()
        qr.saveImageOnDisk("imagem.jpg")

