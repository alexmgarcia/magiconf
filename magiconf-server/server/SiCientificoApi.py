from tastypie.resources import ModelResource
from tastypie.authorization import DjangoAuthorization
from tastypie import fields
from rauth import OAuth2Service
from server.models import Participant,Contact, City, Sight, Restaurant, Hotel, Conference, Event, TalkSession, Author, PosterSession,KeynoteSession,Publication,Article,OrganizationMember,Poster,Notification,NotPresentedPublication,KeynoteSpeaker,Sponsor
from django.contrib.auth.models import User
from sys import stderr

#API to SIORG+Pub
        
"""
Notes: By Default when listing all the entries of a Resource (ex. AUthors) the 
limit is 20. This can be overrided using tag limit

"""
import requests


#http://ianalexandr.com/blog/building-a-true-oauth-20-api-with-django-and-tasty-pie.html

class SICientificoAPI:
    
    '''siorgpub = OAuth2Service(
    client_id='c90f2024b9f231f5094c',
    client_secret='306a106852e55cf5380c29ac14ffe2c28f30c765',
    name='SIM',
    authorize_url='http://127.0.0.2:8000/oauth2/authorize',
    access_token_url='http://127.0.0.2:8000/oauth2/access_token',
    base_url='http://127.0.0.2:8000/')'''

    params = {'redirect_uri': 'http://127.0.0.2:8000/','response_type': 'code'}
    
    baseUrl= 'http://127.0.0.2:8000/api/v1/'

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
    
    def getAuthorInfo(self,authorEmail=None,insertOnBd=False):
        payload = {'format': 'json','email':authorEmail}
        r = requests.get(self.baseUrl+ 'author/', params=payload)
        print r.status_code
        aux = r.json()['objects']
        if len(aux) == 0 :
            stderr.write('Author with email: \"'+authorEmail + '\" doesn\'t exist in SIOrg+Pub')
            return None
        
        print aux
        data = aux[0]
        
        a = Author(name=data['name'],email=data['email'],work_place=data['work_place'],country=data['country'],photo=data['photo'])
        
        part = data.get('participant')
        if (part != None) & (part != 'None') :
            a.participant = data['participant']
        contact = data.get('contact')     
        if (contact != None) & (contact != 'None') :
            a.contact = data['contact']
        
        if insertOnBd :
            a.save()
        return a
    
        
    
    def getAuthors(self,limit=0,offset=-1):

        payload = {'format': 'json',}
        if limit > 0 :
            payload['limit']=limit
        if offset >= 0 :
            payload['offset']= offset
            
        r = requests.get(self.baseUrl+ 'author/', params=payload)
        print r.status_code
        
        aux = r.json()['objects']
        print aux, len(aux)
        if len(aux) == 0 :
            stderr.write('No results returned from SIOrg+Pub')
            return None
        
        return aux

    def getArticlesFromAuthor(self,authorEmail,limit=0,offset=-1):
        payload = {'format': 'json','authors__email':authorEmail}
       
        if limit > 0 :
            payload['limit']=limit
        if offset >= 0 :
            payload['offset']= offset
            
        r = requests.get(self.baseUrl+ 'article/', params=payload)
        print r.status_code
        
        aux = r.json()['objects']
        print aux, len(aux)
        if len(aux) == 0 :
            stderr.write('No results returned from SICientifico')
            return None
        
        return aux 
    
    def getArticleInfo(self,title=None,insertOnBd=False):
        payload = {'format': 'json','title':title}
        r = requests.get(self.baseUrl+ 'article/', params=payload)
        print r.status_code
        
        aux = r.json()['objects']
        if len(aux) == 0 :
            stderr.write('Article with title: \"'+title + '\" doesn\'t exist in SICientifico')
            return None
        
        data = aux[0]
        article = Article(title= data['title'], abstract=data['abstract'])
        
        if insertOnBd :
            authors = data['authors']
            for author in authors:
                a = self.getAuthorInfo(author['email'], insertOnBd)
                article.save()
                article.authors.add(a)
            return article, None # TODO descrever na doc da api este caso de nao devolver json
    
        return article, data
    
    def getArticles(self,limit=0,offset=-1):
        payload = {'format': 'json',}
       
        if limit > 0 :
            payload['limit']=limit
        if offset >= 0 :
            payload['offset']= offset
            
        r = requests.get(self.baseUrl+ 'article/', params=payload)
        print r.status_code
        
        aux = r.json()['objects']
        print aux, len(aux)
        if len(aux) == 0 :
            stderr.write('No results returned from SICientifico')
            return None
        
        return aux 
    
    def getHotelInfo(self,name=None,insertOnBd=False):
        payload = {'format': 'json','name':name}
        r = requests.get(self.baseUrl+ 'hotel/', params=payload)
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
        r = requests.get(self.baseUrl+ 'restaurant/', params=payload)
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
            
        r = requests.get(self.baseUrl+ 'poster/', params=payload)
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
            
        r = requests.get(self.baseUrl+ 'keynotespeaker/', params=payload)
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
            
        r = requests.get(self.baseUrl+ 'participant/', params=payload)
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
            
        r = requests.get(self.baseUrl+ 'sight/', params=payload)
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
            
        r = requests.get(self.baseUrl+ 'hotel/', params=payload)
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
            
        r = requests.get(self.baseUrl+ 'restaurant/', params=payload)
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
            
        r = requests.get(self.baseUrl+ 'sponsor/', params=payload)
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
            
        r = requests.get(self.baseUrl+ 'organizationmember/', params=payload)
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
            
        r = requests.get(self.baseUrl+ 'event/', params=payload)
        print r.status_code
        
        aux = r.json()['objects']
        print aux, len(aux)
        if len(aux) == 0 :
            stderr.write('No results returned from SIOrg+Pub')
            return None
        
        return aux
        

      
        
        
        