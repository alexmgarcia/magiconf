from tastypie.resources import ModelResource
from tastypie.authorization import DjangoAuthorization
from tastypie import fields
#from rauth import OAuth2Service
from server.models import Participant,Contact, City, Sight, Restaurant, Hotel, Conference, Event, TalkSession, Author, PosterSession,KeynoteSession,Publication,Article,OrganizationMember,Poster,Notification,NotPresentedPublication,KeynoteSpeaker,Sponsor
from django.contrib.auth.models import User
from sys import stderr
import urllib2
from urlparse import urlparse
from django.core.files import File
from django.core.files.temp import NamedTemporaryFile


#API to SIORG+Pub
        
"""
Notes: By Default when listing all the entries of a Resource (ex. AUthors) the 
limit is 20. This can be overrided using tag limit

"""
import requests


#http://ianalexandr.com/blog/building-a-true-oauth-20-api-with-django-and-tasty-pie.html

class SIOrgPubAPI:
    
    """siorgpub = OAuth2Service(
    client_id='c90f2024b9f231f5094c',
    client_secret='306a106852e55cf5380c29ac14ffe2c28f30c765',
    name='SIM',
    authorize_url='http://127.0.0.2:8000/oauth2/authorize',
    access_token_url='http://127.0.0.2:8000/oauth2/access_token',
    base_url='http://127.0.0.2:8000/')"""

    params = {'redirect_uri': 'http://127.0.0.2:8000/','response_type': 'code'}
    
    baseUrlAPI= 'http://127.0.0.2:8000/api/v1/'
    baseUrl= 'http://127.0.0.2:8000/'
    baseUrlNoSlash= 'http://127.0.0.2:8000'

    data = {
        'grant_type': 'password',
        'username': 'SIM',
        'password': 'si',
        'scope':'read',
        'redirect_uri': 'http://127.0.0.2:8000/api/'}
   
        
    def initOAUTH(self):
    # the return URL is used to validate the request
        #url = self.siorgpub.get_authorize_url(**self.params)
        #print(url)
        #self.session = self.siorgpub.get_auth_session(data=self.data)
        #self.session = self.siorgpub.get_auth_session(data={'code': 'foo','redirect_uri': redirect_uri})

        #print self.session.get('me').json()['username']
        #return True
        pass

    def getConferenceInfo(self,confName=None,insertOnBd=False):
        payload = {'format': 'json','name' :confName,}
        r = requests.get(self.baseUrlAPI+ 'conference/', params=payload)
        print r.status_code
        data = r.json()['objects'][0]
        #print data
        
        aux = data['city'].split('/')
        city = aux[len(aux)-2]
        print city
        
        c = self.getCityInfo(city)
        
        #Let's download the logo!
        print data['photo']
        logo,logoname = self.getConferenceLogo(data['photo'])
        
        conf = Conference(name=data['name'],edition=str(data['edition']), place=data['place'],beginning_date=data['beginning_date'],ending_date=data['ending_date'],website=data['website'],description=data['description'],latitude=data['latitude'],longitude=data['longitude'], city= c)
        
        conf.photo.save(logoname, File(logo))
        
        if insertOnBd :
            conf.save()
        
        return conf
        
        
        
    def getCityInfo(self,cityName=None,insertOnBd=False):
        payload = {'format': 'json','name':cityName}
        r = requests.get(self.baseUrlAPI+ 'city/', params=payload)
        print r.status_code
        data = r.json()['objects'][0]
        #print data
        
        c = City(name=data['name'],description=data['description'],photo=data['photo'])
        if insertOnBd :
            c.save()
        return c
    

    
    def getParticipantInfo(self,participantUsername,insertOnBd=False):
        payload = {'format': 'json','username':participantUsername}
        r = requests.get(self.baseUrlAPI+ 'participant/', params=payload)
        print r.status_code
        print r.json()
        aux = r.json()['objects']
        if len(aux) == 0 :
            stderr.write('Participant with username: \"'+participantUsername + '\" doesn\'t exist in SIOrg+Pub')
            return None
        
        data = aux[0]
        contact= data['contact']['id']
        c = Contact(id = contact)
        p = Participant(username=data['username'],password=data['password'],phone_number=data['phone_number'],qrcode=data['qrcode'],name=data['name'],email=data['email'],work_place=data['work_place'],country=data['country'],photo=data['photo'],contact=c)
        
        if insertOnBd :
            p.save()
        return p
    
    
    def getKeynoteSpeakerInfo(self,keynoteEmail=None,insertOnBd=False):
        payload = {'format': 'json','email':keynoteEmail}
        r = requests.get(self.baseUrlAPI+ 'keynotespeaker/', params=payload)
        print r.status_code
        aux = r.json()['objects']
        if len(aux) == 0 :
            stderr.write('Keynote with email: \"'+keynoteEmail + '\" doesn\'t exist in SIOrg+Pub')
            return None
        
        print aux
        data = aux[0]
        
        a = KeynoteSpeaker(name=data['name'],email=data['email'],work_place=data['work_place'],country=data['country'],photo=data['photo'])
        
        part = data.get('participant')
        if (part != None) & (part != 'None') :
            a.participant = data['participant']
        contact = data.get('contact')     
        if (contact != None) & (contact != 'None') :
            a.contact = data['contact']
        author = data.get('author')
        if (author != None) & (author != 'None')  :
            a.author = data['author']
        
        if insertOnBd :
            a.save()
        return a
    
    def getEventInfo(self,title=None,insertOnBd=False):
        payload = {'format': 'json','title':title}
        r = requests.get(self.baseUrlAPI+ 'event/', params=payload)
        print r.status_code
        aux = r.json()['objects']
        if len(aux) == 0 :
            stderr.write('Event with title: \"'+title + '\" doesn\'t exist in SIOrg+Pub')
            return None
        
        #print aux
        data = aux[0]
        conference = data['conference']
        
        city_aux = conference['city'].split('/')
        city_name = city_aux[len(city_aux)-2]
        print city_name
        
        c = self.getCityInfo(city_name,insertOnBd)
        conf = None
        
        try:
            localConf = Conference.objects.get(name=conference['name'],edition=str(conference['edition']), place=conference['place'])
            conf = localConf
        except Conference.DoesNotExist:
            conf = Conference(name=conference['name'],edition=str(conference['edition']), place=conference['place'],beginning_date=conference['beginning_date'],ending_date=conference['ending_date'],website=conference['website'],description=conference['description'],latitude=conference['latitude'],longitude=conference['longitude'], city= c)
            conf.save()
        
        print 'local conf %s' % conf
        
        
        e = None
        if data.get('keynotes') != None :
            try:
                print 'keynotesession'
                KeynoteSession.objects.get(title=data['title'],time=data['time'])
            except Exception:
                e = KeynoteSession(title=data['title'],place=data['place'],time=data['time'],duration=data['duration'],description=data['description'],conference= conf)
        elif data.get('articles') != None :
            try:
                print 'talksession'
                TalkSession.objects.get(title=data['title'],time=data['time'])
            except Exception:
                e = TalkSession(title=data['title'],place=data['place'],time=data['time'],duration=data['duration'],conference= conf)
        elif data.get('posters') != None :
            try:
                print 'postersession'
                PosterSession.objects.get(title=data['title'],time=data['time'])
            except Exception:
                e = PosterSession(title=data['title'],place=data['place'],time=data['time'],duration=data['duration'],conference= conf)
        
        if insertOnBd & (e != None ) :
            e.save()
            
        return e
    
    
    def getPosterInfo(self,posterTitle=None,insertOnBd=False):
        
        payload = {'format': 'json','title':posterTitle}
        r = requests.get(self.baseUrlAPI+ 'poster/', params=payload)
        print r.status_code
        
        aux = r.json()['objects']
        if len(aux) == 0 :
            stderr.write('Poster with title: \"'+posterTitle + '\" doesn\'t exist in SIOrg+Pub')
            return None
        
        data = aux[0]
        
        p = Poster(title=data['title'])
        
        #This can be optimized: http://stackoverflow.com/questions/6996176/how-to-create-an-object-for-a-django-model-with-a-many-to-many-field
        #for author in data['authors'] :
        #    a = self.getAuthorInfo(author['name'])
        #   p.authors.add(a)
                        
        if insertOnBd :
            p.save()
        return p,data['authors']
    
    def getOrganizationMemberInfo(self,email=None,insertOnBd=False):
        payload = {'format': 'json','email':email}
        r = requests.get(self.baseUrlAPI+ 'organizationmember/', params=payload)
        print r.status_code
        
        aux = r.json()['objects']
        if len(aux) == 0 :
            stderr.write('Organization Member with email: \"'+email + '\" doesn\'t exist in SIOrg+Pub')
            return None
        
        data = aux[0]
        
        aux_conference = data['conference'].split('/')
        conference_id = aux_conference[len(aux_conference)-2]
        print "Conference id %s" % conference_id
        
       
        
        conference = Conference.objects.get(pk=conference_id)
        if conference != None  :
            m = OrganizationMember(name= data['name'], email= data['email'], photo =data['photo'] , role =data['role'], work_place=data['work_place'], country=data['country'], conference=conference)
            if insertOnBd :
                m.save()
        else:
            stderr.write('No Conference entry with id '+ conference_id + ' exists in SI-M database')
            return None
    
        return m,conference
    
    def getHotelInfo(self,name=None,insertOnBd=False):
        payload = {'format': 'json','name':name}
        r = requests.get(self.baseUrlAPI+ 'hotel/', params=payload)
        print r.status_code
        
        aux = r.json()['objects']
        if len(aux) == 0 :
            stderr.write('Hotel with name: \"'+name + '\" doesn\'t exist in SIOrg+Pub')
            return None
        
        data = aux[0]
        
        aux_city = data['city'].split('/')
        city_id = aux_city[len(aux_city)-2]
        print "City id %s" % city_id
        
       
        
        city = City.objects.get(pk=city_id)
        if city != None  :
            m = Hotel(name= data['name'], address= data['address'], phone_number=data['phone_number'],stars =data['stars'] , longitude =data['longitude'], latitude=data['latitude'], description=data['description'], city=city)
            if insertOnBd :
                m.save()
        else:
            stderr.write('No City entry with id '+ city_id + ' exists in SI-M database')
            return None
    
        return m,city
    
    def getRestaurantInfo(self,name=None,insertOnBd=False):
        payload = {'format': 'json','name':name}
        r = requests.get(self.baseUrlAPI+ 'restaurant/', params=payload)
        print r.status_code
        
        aux = r.json()['objects']
        if len(aux) == 0 :
            stderr.write('Restaurant with name: \"'+name + '\" doesn\'t exist in SIOrg+Pub')
            return None
        
        data = aux[0]
        
        aux_city = data['city'].split('/')
        city_id = aux_city[len(aux_city)-2]
        print "City id %s" % city_id
        
       
        
        city = City.objects.get(pk=city_id)
        if city != None  :
            m = Restaurant(name= data['name'], address= data['address'], phone_number=data['phone_number'],longitude =data['longitude'], latitude=data['latitude'], description=data['description'], city=city)
            if insertOnBd :
                m.save()
        else:
            stderr.write('No City entry with id '+ city_id + ' exists in SI-M database')
            return None
    
        return m,city
    
    
    
    
    def getPosters(self,limit=0,offset=-1):

        payload = {'format': 'json',}
        if limit > 0 :
            payload['limit']=limit
        if offset >= 0 :
            payload['offset']= offset
            
        r = requests.get(self.baseUrlAPI+ 'poster/', params=payload)
        print r.status_code
        
        aux = r.json()['objects']
        print aux, len(aux)
        if len(aux) == 0 :
            stderr.write('No results returned from SIOrg+Pub')
            return None
        
        return aux
    
    def getKeynoteSpeakers(self,limit=0,offset=-1):

        payload = {'format': 'json',}
        if limit > 0 :
            payload['limit']=limit
        if offset >= 0 :
            payload['offset']= offset
            
        r = requests.get(self.baseUrlAPI+ 'keynotespeaker/', params=payload)
        print r.status_code
        
        aux = r.json()['objects']
        print aux, len(aux)
        if len(aux) == 0 :
            stderr.write('No results returned from SIOrg+Pub')
            return None
        
        return aux
        
        
    def getParticipants(self,limit=0,offset=-1):

        payload = {'format': 'json',}
        if limit > 0 :
            payload['limit']=limit
        if offset >= 0 :
            payload['offset']= offset
            
        r = requests.get(self.baseUrlAPI+ 'participant/', params=payload)
        print r.status_code
        
        aux = r.json()['objects']
        print aux, len(aux)
        if len(aux) == 0 :
            stderr.write('No results returned from SIOrg+Pub')
            return None
        
        return aux
    
    def getSights(self,limit=0,offset=-1):

        payload = {'format': 'json',}
        if limit > 0 :
            payload['limit']=limit
        if offset >= 0 :
            payload['offset']= offset
            
        r = requests.get(self.baseUrlAPI+ 'sight/', params=payload)
        print r.status_code
        
        aux = r.json()['objects']
        print aux, len(aux)
        if len(aux) == 0 :
            stderr.write('No results returned from SIOrg+Pub')
            return None
        
        return aux
    
    def getHotels(self,limit=0,offset=-1):

        payload = {'format': 'json',}
        if limit > 0 :
            payload['limit']=limit
        if offset >= 0 :
            payload['offset']= offset
            
        r = requests.get(self.baseUrlAPI+ 'hotel/', params=payload)
        print r.status_code
        
        aux = r.json()['objects']
        print aux, len(aux)
        if len(aux) == 0 :
            stderr.write('No results returned from SIOrg+Pub')
            return None
        
        return aux
    
    def getRestaurants(self,limit=0,offset=-1):

        payload = {'format': 'json',}
        if limit > 0 :
            payload['limit']=limit
        if offset >= 0 :
            payload['offset']= offset
            
        r = requests.get(self.baseUrlAPI+ 'restaurant/', params=payload)
        print r.status_code
        
        aux = r.json()['objects']
        print aux, len(aux)
        if len(aux) == 0 :
            stderr.write('No results returned from SIOrg+Pub')
            return None
        
        return aux
    
    
    def getSponsors(self,limit=0,offset=-1):

        payload = {'format': 'json',}
        if limit > 0 :
            payload['limit']=limit
        if offset >= 0 :
            payload['offset']= offset
            
        r = requests.get(self.baseUrlAPI+ 'sponsor/', params=payload)
        print r.status_code
        
        aux = r.json()['objects']
        print aux, len(aux)
        if len(aux) == 0 :
            stderr.write('No results returned from SIOrg+Pub')
            return None
        
        return aux

    def getOrganizationMembers(self,limit=0,offset=-1):

        payload = {'format': 'json',}
        if limit > 0 :
            payload['limit']=limit
        if offset >= 0 :
            payload['offset']= offset
            
        r = requests.get(self.baseUrlAPI+ 'organizationmember/', params=payload)
        print r.status_code
        
        aux = r.json()['objects']
        print aux, len(aux)
        if len(aux) == 0 :
            stderr.write('No results returned from SIOrg+Pub')
            return None
        
        return aux
        
    def getEvents(self,limit=0,offset=-1,day=None):

        payload = {'format': 'json',}
        if limit > 0 :
            payload['limit']=limit
        if offset >= 0 :
            payload['offset']= offset
        if day != None:
            payload['time__startswith']=day
            
        r = requests.get(self.baseUrlAPI+ 'event/', params=payload)
        print r.status_code
        
        aux = r.json()['objects']
        print aux, len(aux)
        if len(aux) == 0 :
            stderr.write('No results returned from SIOrg+Pub')
            return None
        
        return aux
        

    def getConferenceLogo(self,photoURL=None) :
        """url = self.baseUrl+photoURL
        result = urllib.urlretrieve(url) # image_url is a URL to an image
        print result[0]
        f = open(result[0], 'w+')
        logo = File(f)"""
        
        """
        name = urlparse(img_url).path.split('/')[-1]

        # See also: http://docs.djangoproject.com/en/dev/ref/files/file/
        photo.image.save(name, File(urllib2.urlopen(self.url).read(), save=True)"""
        
        url = self.baseUrlNoSlash + photoURL
        
        """#name = urlparse(url).path.split('/')[-1]
        name = urlparse(photoURL).path.split('/')[-1]
        logo = File(urllib2.urlopen(url).read(),name)"""
        
        file_temp = NamedTemporaryFile()
        file_temp.write(urllib2.urlopen(url).read())
        file_temp.flush()
        
        name = urlparse(photoURL).path.split('/')[-1]
        print "Name: %s" % name
        
        return file_temp, name
          
    def getNotifications(self,limit=0,offset=-1,day=None):

        payload = {'format': 'json',}
        if limit > 0 :
            payload['limit']=limit
        if offset >= 0 :
            payload['offset']= offset
        if day != None:
            payload['date__startswith']=day
            
        r = requests.get(self.baseUrlAPI+ 'notification/', params=payload)
        print r.status_code
        
        aux = r.json()['objects']
        print aux, len(aux)
        if len(aux) == 0 :
            stderr.write('No results returned from SIOrg+Pub')
            return None
        
        return aux
    
    def getNotification(self,title=None,date=None,insertOnBd=False):
        payload = {'format': 'json','title':title,'date':date}
        r = requests.get(self.baseUrlAPI+ 'notification/', params=payload)
        print r.status_code
        aux = r.json()['objects']
        if len(aux) == 0 :
            stderr.write('Notification with title: \"'+title + '\" and date: \"' + str(date) + '\" doesn\'t exist in SIOrg+Pub')
            return None
        
        print aux
        data = aux[0]
        
        a = Notification(title=data['title'],description=data['description'],date=data['date'])
        
        if insertOnBd :
            a.save()
        return a
        