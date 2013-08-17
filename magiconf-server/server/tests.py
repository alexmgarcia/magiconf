"""
This file demonstrates writing tests using the unittest module. These will pass
when you run "manage.py test".

Replace this with more appropriate tests for your application.
"""

#RUN: python manage.py test server

from tastypie.test import ResourceTestCase
from server.models import Author,Sight,Participant,KeynoteSpeaker,Article
from django.test import TestCase
from server.SiOrgPubApi import SIOrgPubAPI
from server.SiCientificoApi import SICientificoAPI
from django.test.client import Client
from django.contrib.auth.models import User


class AuthorResourceTest(ResourceTestCase):
    
    TOTAL_AUTHORS = Author.objects.count()
    
    def setUp(self):
        super(AuthorResourceTest, self).setUp()
        
        self.author = Author(work_place='place1', photo='photo.jpg', name='Author 2', country='Country 2', email='email')
        
        self.post_data = {
            'email': self.author.email,
            'name': self.author.name,
            'photo': self.author.photo,
            'work_place': self.author.work_place,
            'country': self.author.country
            }
        
        self.detail_url = '/api/v1/author/{0}/'.format(self.author.pk)
        
    def test_post_json(self):
        self.assertEqual(Author.objects.count(), self.TOTAL_AUTHORS)
        self.assertHttpCreated(self.api_client.post('/api/v1/author/', format='json', data=self.post_data))
        self.assertEqual(Author.objects.count(), self.TOTAL_AUTHORS+1)
    
    def test_get_json(self):
        self.assertHttpCreated(self.api_client.post('/api/v1/author/', format='json', data=self.post_data))
        resp = self.api_client.get(self.detail_url, format='json')
        self.assertValidJSONResponse(resp)
          
class APISIOrgTest(TestCase):
    
    fixtures = ['initial_data.json']
    siorg = None
        
    LIMIT = 20
    
    def setUp(self):
        self.siorg = SIOrgPubAPI()
    
    def testGetSights(self):
        sights = Sight.objects.all()
        totalSights = len(sights)
        print totalSights
        times = totalSights / self.LIMIT;
        last = totalSights % self.LIMIT;
        
        offset = 0
        
        while times > 0 :
            result = self.siorg.getSights(self.LIMIT, offset)
            if result != None:
                result_len = len(result)
                times = times - 1
                offset = offset+ result_len
        
        result = self.siorg.getSights(last, offset)
        if result != None:
            result_len = len(result)
            offset = offset+ result_len
            
            
        self.assertEqual(totalSights, offset , "Number of sights returned by the API call must be equal to the number of sights in BD.")
        
    def testGetParticipants(self):
        participants = Participant.objects.all()
        totalParticipants = len(participants)
        print totalParticipants
        times = totalParticipants / self.LIMIT;
        last = totalParticipants % self.LIMIT;
        
        offset = 0
        
        while times > 0 :
            result = self.siorg.getParticipants(self.LIMIT, offset)
            if result != None:
                result_len = len(result)
                times = times - 1
                offset = offset+ result_len
        
        result = self.siorg.getParticipants(last, offset)
        if result != None:
            result_len = len(result)
            offset = offset+ result_len
            
            
        self.assertEqual(totalParticipants, offset , "Number of participants returned by the API call must be equal to the number of participants in BD.")
        
    def testGetKeynoteSpeakers(self):
        keynote_speakers = KeynoteSpeaker.objects.all()
        totalKeynoteSpeakers = len(keynote_speakers)
        print totalKeynoteSpeakers
        times = totalKeynoteSpeakers / self.LIMIT;
        last = totalKeynoteSpeakers % self.LIMIT;
        
        offset = 0
        
        while times > 0 :
            result = self.siorg.getKeynoteSpeakers(self.LIMIT, offset)
            if result != None:
                result_len = len(result)
                times = times - 1
                offset = offset+ result_len
        
        result = self.siorg.getKeynoteSpeakers(last, offset)
        if result != None:
            result_len = len(result)
            offset = offset+ result_len
            
        self.assertEqual(totalKeynoteSpeakers, offset , "Number of keynote_speakers returned by the API call must be equal to the number of keynote_speakers in BD.") 
        
        
        
class APISICientificoTest(TestCase):
    
    fixtures = ['initial_data.json']
    sicientifico = None
        
    LIMIT = 20
    
    def setUp(self):
        self.sicientifico = SICientificoAPI()
    
    def testGetArticles(self):
        articles = Article.objects.all()
        totalArticles = len(articles)
        times = totalArticles / self.LIMIT;
        last = totalArticles % self.LIMIT;
        
        offset = 0
        
        while times > 0 :
            result = self.sicientifico.getArticles(self.LIMIT, offset)
            if result != None:
                result_len = len(result)
                times = times - 1
                offset = offset+ result_len
        
        result = self.sicientifico.getArticles(last, offset)
        if result != None:
            result_len = len(result)
            offset = offset+ result_len
            
            
        self.assertEqual(totalArticles, offset , "Number of articles returned by the API call must be equal to the number of articles in BD.")
        
        
    def testGetAuthors(self):
        authors = Author.objects.all()
        totalAuthors = len(authors)
        print totalAuthors
        times = totalAuthors / self.LIMIT;
        last = totalAuthors % self.LIMIT;
        
        offset = 0
        
        while times > 0 :
            result = self.sicientifico.getAuthors(self.LIMIT, offset)
            if result != None:
                result_len = len(result)
                times = times - 1
                offset = offset+ result_len
        
        result = self.sicientifico.getAuthors(last, offset)
        if result != None:
            result_len = len(result)
            offset = offset+ result_len
            
            
        self.assertEqual(totalAuthors, offset , "Number of authors returned by the API call must be equal to the number of authors in BD.")
        
    def testGetArticleInfo(self):
        article = self.sicientifico.getArticleInfo('A study on the usage of third party libraries in Java applications', False)[0]
            
        self.assertEqual(article.title, 'A study on the usage of third party libraries in Java applications' , "Article title is not correct")
    
    def testGetArticlesFromAuthor(self):
        articles_author = len(self.sicientifico.getArticlesFromAuthor('joao.cachopo@ist.utl.pt'))
        self.assertEqual(articles_author, 1 , "The number of articles returned isn't the expected")

class PINExchangeTest(TestCase):
    def setUp(self):
        self.c = Client()
        user = User.objects.create_user(username='test', email='', password='test')
        user.save()
        participant = Participant.objects.create(username='test', password='test', work_place='test', photo='photo', name='user', country='pt', phone_number=123456789, email='user@email.com')
        participant.save()
        
    def testGenPIN(self):
        response = self.c.get('/api/v1/pin/generate/?user_name=test&password=test')
        self.assertNotEqual(response, 'Unauthorized', 'Expected a response different from Unauthorized')
        self.assertEqual(response.status_code, 200, 'Expected 200 status code')
        
    def testGenPINAuthorization(self):
        response = self.c.get('/api/v1/pin/generate/?user_name=test2&password=test')
        self.assertEquals(response.status_code, 401, 'Expected 401 status code')
        
    def testRemovePIN(self):
        self.c.get('/api/v1/pin/remove/?user_name=test&password=test')
        self.assertNotEqual(response, 'Unauthorized', 'Expected a response different from Unauthorized')
        self.assertEqual(response.status_code, 200, 'Expected 200 status code')
        
    def testRemovePINAuthorization(self):
        self.c.get('/api/v1/pin/remove/?user_name=test2&password=test')
        self.assertEquals(response.status_code, 401, 'Expected 401 status code')
        
    def testUnexistingPINRemove(self):
        self.c.get('/api/v1/pin/remove/?user_name=test&password=test')
        self.assertEquals(response.status_code, 404, 'Expected 404 status code')