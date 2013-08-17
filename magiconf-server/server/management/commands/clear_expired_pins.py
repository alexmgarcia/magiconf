from django.core.management.base import BaseCommand
from server.models import ExchangePIN
from datetime import datetime, timedelta

class Command(BaseCommand):
    args = '<poll_id poll_id ...>'
    help = 'Clears all contact exchange pin sessions that have expired'

    def handle(self, *args, **options):
        pins = ExchangePIN.objects.all()
        for pin in pins:
            d = timedelta(hours = 1)
            current_date = datetime.now()
            if pin.date_created+d <= current_date:
                pin.delete()