from django.core.management.base import BaseCommand, CommandError
from server.SiOrgPubApi import SIOrgPubAPI
from optparse import make_option
from encodings import ascii
from string import ascii_letters


class Command(BaseCommand):
    args = '<poll_id poll_id ...>'
    help = 'Updates SI-M Database. Retrieves data from SIOrg+Pub'
    option_list = BaseCommand.option_list + (
            make_option('--all','-a', dest='update',
            default=False,
            help='Updates SI-M Database. Retrieves data from SIOrg+Pub'),
        )


    def handle(self, *args, **options):
        siorg = SIOrgPubAPI()
        #if siorg.initOAUTH() :
        #Call all the function to update all the resources
        
        #siorg.getConferenceInfo('Teste3',insertOnBd=False)
        
        #siorg.getPosterInfo('ReactOR')
        #siorg.getAuthorInfo('vmoreira@iscte.pt')
        #siorg.getKeynoteSpeakerInfo('l.cardelli@microsoft.com')
        #siorg.getParticipantInfo('Teste')
        
        #siorg.getEventInfo('Lorenzo Alvisi (UT Austin)',True)
        #print siorg.getEventInfo('Lorenzo A (UT Austin)',True).title
        #siorg.getEventInfo('Sistemas Embebidos e de Tempo-Real 1')
        #siorg.getEventInfo('Sess\xe3o de Posters'.decode('latin1'))
        
        #siorg.getOrganizationMemberInfo('Jorge Cust\xf3dio'.decode('latin1') ,insertOnBd=True)
        #siorg.getHotelInfo('Lisboa Almada Hotel',True)
        #siorg.getRestaurantInfo('Solar Beir\xe3o'.decode('latin1'),True)
        #siorg.getPosters(limit=4,offset=4)
        #siorg.getAuthors(limit=40,offset=4)
        #siorg.getKeynoteSpeakers(limit=40,offset=4)
        #siorg.getParticipants(limit=40,offset=4)
        #siorg.getSights()
        #siorg.getSponsors()
        #siorg.getOrganizationMembers()
        #siorg.getRestaurants()
        #siorg.getHotels()
        #siorg.getEvents(day='2012-09-06',limit = 2)
        
        #siorg.getConferenceLogo("photos/conference_logos/inforum_logo_2.jpg")
        
        #siorg.getNotifications(day='2013-06-17')                
        siorg.getNotification('teste', '2013-06-17T01:13:04', False)
        
        #else:
        #    self.stdout.write('Update Failed! Couldn\'t estabilish an OAUTH connection with SIOrg+Pub')
                    
        self.stdout.write('Successfully Update DB!')