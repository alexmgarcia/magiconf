from django.core.management.base import BaseCommand, CommandError
from server.SiCientificoApi import SICientificoAPI
from optparse import make_option
from encodings import ascii
from string import ascii_letters


class Command(BaseCommand):
    args = '<poll_id poll_id ...>'
    help = 'Updates SI-M Database. Retrieves data from SICientifico'
    option_list = BaseCommand.option_list + (
            make_option('--all','-a', dest='update',
            default=False,
            help='Updates SI-M Database. Retrieves data from SICientifico'),
        )


    def handle(self, *args, **options):
        sicientifico = SICientificoAPI()
        #if siorg.initOAUTH() :
        #Call all the function to update all the resources
        sicientifico.getArticlesFromAuthor('joao.cachopo@ist.utl.pt')
        #print sicientifico.getArticleInfo('gdfgf', True)[0].abstract
        #print sicientifico.getArticles(2, -1)                
        #else:
        #    self.stdout.write('Update Failed! Couldn\'t estabilish an OAUTH connection with SIOrg+Pub')
                    
        self.stdout.write('Successfully Update DB!')