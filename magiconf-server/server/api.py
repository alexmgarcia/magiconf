from tastypie.resources import ModelResource, Resource
from tastypie.authorization import DjangoAuthorization, Authorization
from tastypie import fields
from tastypie.resources import ALL
from tastypie.http import HttpNotFound
from server.models import Participant,Contact, City, Sight, Restaurant, Hotel, Conference, Event, TalkSession, Author, PosterSession,KeynoteSession,WorkshopSession,Publication,Article,OrganizationMember,Poster,Notification,NotPresentedPublication,KeynoteSpeaker,Sponsor,ExchangePIN, ParticipantExchangeEmailRelationship,ParticipantResetPassword,SocialEvent
from tastypie.paginator import Paginator
from server.utils.jsonSerializer import PrettyJSONSerializer 
from tastypie.authentication import BasicAuthentication, Authentication
from server.authentication import CustomAuthentication
from django.contrib.auth.models import User
from django.conf.urls import *
import json
from django.core.exceptions import ObjectDoesNotExist
from django.core.mail import send_mail
from server.authentication import *
from django.db.models import Q
from magiconf.settings import SERVER_ADDRESS
import smtplib
import traceback
from datetime import datetime


        
class ContactResource(ModelResource):
    class Meta:
        include_resource_uri = False
        queryset = Contact.objects.all()
        paginator_class = Paginator
        serializer = PrettyJSONSerializer()
        authentication = CustomAuthentication()
        authorization = DjangoAuthorization()
        list_allowed_methods = ['get', 'post']
        resource_name = 'contact'
        """authentication = OAuth20Authentication()"""
        
    def get_object_list(self, request):
           
        list = super(ContactResource, self).get_object_list(request)
        if request.method == "GET":
            params = request.GET
            uname = params['user_name']
            print "Username: %s" % uname
            user = User.objects.get(username__exact=uname)
            if user.is_superuser :
                return list
        return []
        
    def prepend_urls(self):
        return [
               url(r"^(?P<resource_name>%s)/add/$" % self._meta.resource_name,
                   self.wrap_view('add_contact'), name="add_contact"),
               url(r"^(?P<resource_name>%s)/remove/$" % self._meta.resource_name,
                   self.wrap_view('remove_contact'), name="remove_contact"),
               url(r"^(?P<resource_name>%s)/allow_exchange/$" % self._meta.resource_name,
                   self.wrap_view('allow_exchange'), name="allow_exchange"),
                url(r"^(?P<resource_name>%s)/add_author_keynote/$" % self._meta.resource_name,
                   self.wrap_view('add_author_keynote'), name="add_author_keynote"),
               url(r"^(?P<resource_name>%s)/add_email/$" % self._meta.resource_name,
                   self.wrap_view('add_contact_email'), name="add_contact_email"),
           ]
    
    def allow_exchange(self, request, **kwargs):
        self.method_check(request, allowed=['post'])
        self.is_authenticated(request)
        self.throttle_check(request)
        r = json.loads(request.body)
        try:
            participant_adding = Participant.objects.get(username = r.get('username_adding'))
            pin_owner = Participant.objects.get(username = r.get('user_name'))
            if ExchangePIN.objects.get(owner = pin_owner) != None:
                contact_participant_adding = participant_adding.contact
                contact_pin_owner = pin_owner.contact
                if pin_owner.contacts.get(id = contact_participant_adding.id) != None:
                    participant_adding.contacts.add(contact_pin_owner)
                    participant_adding_device = participant_adding.device
                    participant_adding_response = {'contact': 
                            {'username' : pin_owner.username,
                           'name' : pin_owner.name,
                           'work_place' : pin_owner.work_place,
                           'photo' : (pin_owner.photo.url if pin_owner.photo else ''),
                           'country' : pin_owner.country,
                           'phone_number' : pin_owner.phone_number,
                           'email' : pin_owner.email,
                           'facebook_url' : pin_owner.facebook_url,
                           'linkedin_url' : pin_owner.linkedin_url                           
                           } 
                        }
                    participant_adding_device.send_message("[CONTACT_EXCHANGE_DATA]" + json.dumps(participant_adding_response))
                    response = {'exchange' : 'allowed'}
                    return self.create_response(request, response)
        except Contact.DoesNotExist:
            if contact_pin_owner not in participant_adding.contacts.filter():
                participant_adding.contacts.add(contact_pin_owner)
                participant_adding_device = participant_adding.device
                participant_adding_response = {'contact': 
                            {'username' : pin_owner.username,
                           'name' : pin_owner.name,
                           'work_place' : pin_owner.work_place,
                           'photo' : (pin_owner.photo.url if pin_owner.photo else ''),
                           'country' : pin_owner.country,
                           'phone_number' : pin_owner.phone_number,
                           'email' : pin_owner.email,
                           'facebook_url' : pin_owner.facebook_url,
                           'linkedin_url' : pin_owner.linkedin_url                           
                           } 
                        }
                if participant_adding_device != None:
					participant_adding_device.send_message("[CONTACT_EXCHANGE_DATA]" + json.dumps(participant_adding_response))
                else:
                    raise Participant.DoesNotExist
                
            pin_owner.contacts.add(contact_participant_adding)
            
            response = {'contact': 
							{'username' : participant_adding.username,
                           'name' : participant_adding.name,
                           'work_place' : participant_adding.work_place,
                           'photo' : (participant_adding.photo.url if participant_adding.photo else ''),
                           'country' : participant_adding.country,
                           'phone_number' : participant_adding.phone_number,
                           'email' : participant_adding.email,
                           'facebook_url' : pin_owner.facebook_url,
                           'linkedin_url' : pin_owner.linkedin_url							   
						   }
						}
            return self.create_response(request, response)
        except ExchangePIN.DoesNotExist:
            response = {'error' : 'Pin not found'}
            return self.create_response(request, response, response_class=HttpNotFound)
        except Participant.DoesNotExist:
            response = {'error' : 'Participant not found'}
            return self.create_response(request, response, response_class=HttpNotFound)
        
    def add_contact(self, request, **kwargs):
        self.method_check(request, allowed=['post'])
        self.is_authenticated(request)
        self.throttle_check(request)
        r = json.loads(request.body)
        try:
            participant_adding = Participant.objects.get(username = r.get('user_name'))
            pin_owner = ExchangePIN.objects.get(pin=r.get('pin')).owner
            if participant_adding == pin_owner:
                response = {'error' : 'Adding yourself'}
                return self.create_response(request, response)
            pin_owner_device = pin_owner.device
            contact_pin_owner = pin_owner.contact
            contact_participant_adding = participant_adding.contact
            participant_adding_response = {'contact':
						{'username' : pin_owner.username,
                           'name' : pin_owner.name,
                           'work_place' : pin_owner.work_place,
                           'photo' : (pin_owner.photo.url if pin_owner.photo else ''),
                           'country' : pin_owner.country,
                           'phone_number' : pin_owner.phone_number,
                           'email' : pin_owner.email,
                           'facebook_url' : pin_owner.facebook_url,
                           'linkedin_url' : pin_owner.linkedin_url							   
						}
					}
            if participant_adding.contacts.get(id = contact_pin_owner.id) != None:
                if pin_owner.contacts.get(id = contact_participant_adding.id) != None:
                    response = {'error' : 'Already in contacts'}
        except Contact.DoesNotExist:
            pin_owner_device.send_message("[CONTACT_EXCHANGE_REQUEST]Participant_name:" + participant_adding.name + "|Participant_username:" + participant_adding.username)
            response = {'request':'sent'}
        except ExchangePIN.DoesNotExist:
            response = {'error':'Pin not found'}
        finally:
            return self.create_response(request, response)
       
    def remove_contact(self, request, **kwargs):
        self.method_check(request, allowed=['post'])
        self.is_authenticated(request)
        self.throttle_check(request)
        r = json.loads(request.body)
        try:
            participant_removing = Participant.objects.get(username = r.get('user_name'))
            contact_remove = Participant.objects.get(email=r.get('email_remove')).contact
            if participant_removing.contacts.get(id = contact_remove.id) != None:
                participant_removing.contacts.remove(contact_remove)
                response = {"removed" : r.get('email_remove') }
            return self.create_response(request, response)
        except Participant.DoesNotExist:
            response = {'error' : 'Participant not found'}
            return self.create_response(request, response, response_class=HttpNotFound)
        except Contact.DoesNotExist:
            response = {'error' : 'Not in contacts'}
            return self.create_response(request, response)
        
        
        
        
    def add_author_keynote(self, request, **kwargs):
        self.method_check(request, allowed=['post'])
        self.is_authenticated(request)
        self.throttle_check(request)
        r = json.loads(request.body)
        print r
        try:
            participant_adding = Participant.objects.get(username = r.get('user_name')) 
            typeResource = r.get('type')
            participant_to_add = None
            if typeResource == "author" :
                participant_to_add = Author.objects.get(email=r.get('email')).participant
            elif typeResource == "keynote":
                participant_to_add = KeynoteSpeaker.objects.get(email=r.get('email')).participant
                
            if participant_adding == participant_to_add :
                response = {'error' : 'Adding yourself'}
                return self.create_response(request, response)
            try:
                participant_adding.contacts.get(id = participant_to_add.contact.id) != None
                response = {'error' : 'Already in contacts'}
                return self.create_response(request, response)
            except Contact.DoesNotExist:
                print ""
                #Do nothing
            #Create entry in BD
            exchange_request = ParticipantExchangeEmailRelationship()
            exchange_request.token_id = createSaltedMD5Hash(participant_adding.username + participant_to_add.username)
            exchange_request.participant_sent_request = participant_adding
            exchange_request.participant_received_request = participant_to_add
            print "before email"
            try:
                status = self.send_exchange_email_request(participant_adding, exchange_request, participant_to_add.email)
            except :
                print "caught"
            if status == 1:
                exchange_request.save()
                response = {'request' : 'sent'}
            else:
                response = {'error' : 'Error while sending email'}
        except Contact.DoesNotExist:
            response = {'error' : 'Participant not found'}
        except Participant.DoesNotExist:
            response = {'error' : 'Participant not found'}
            return self.create_response(request, response, response_class=HttpNotFound)
        except Author.DoesNotExist:
            response = {'error' : 'Invalid Author'}
        except KeynoteSpeaker.DoesNotExist:
            response = {'error' : 'Invalid KeynoteSpeaker'}
        except:
            response = {'error' : 'Participant not found'}
        finally:
            return self.create_response(request, response)
        
    
            
    def send_exchange_email_request(self, participant_adding, exchange_request, email):
            return send_mail('Pedido de troca de contactos', 'O participante ' + participant_adding.name + u' pretende adicion\xe1-lo aos seus contactos.\n' + 
                         'Para confirmar a troca visite o seguinte link http://'+SERVER_ADDRESS+'/confirm_contact_exchange/' +
                  exchange_request.token_id+'/', 'django.app@gmail.com', [email], fail_silently=True)
            
    def add_contact_email(self, request, **kwargs):
        self.method_check(request, allowed=['post'])
        self.is_authenticated(request)
        self.throttle_check(request)
        r = json.loads(request.body)
        try:
            participant_adding = Participant.objects.get(username = r.get('user_name'))
            email_owner = Participant.objects.get(email=r.get('email'))
            if participant_adding == email_owner:
                response = {'error' : 'Adding yourself'}
                return self.create_response(request, response)
            if email_owner.contact in participant_adding.contacts.filter():
                response = {'error' : 'Already in contacts'}
                return self.create_response(request, response)
            if ParticipantExchangeEmailRelationship.objects.filter(Q(participant_sent_request__username = participant_adding.username) & 
                                    Q(participant_received_request__username = email_owner.username)).count() > 0:
                response = {'error' : 'Already sent request'}
                return self.create_response(request, response)
            else:
                exchange_request = ParticipantExchangeEmailRelationship()
                exchange_request.token_id = createSaltedMD5Hash(participant_adding.username + email_owner.username)
                exchange_request.participant_sent_request = participant_adding
                exchange_request.participant_received_request = email_owner
                res = self.send_exchange_email_request(participant_adding, exchange_request, email_owner.email)
                if res == 1:
                    exchange_request.save()
                    response = {'request' : 'sent'}
                else:
                    response = {'error' : 'Error while sending email'}
        except Participant.DoesNotExist:
            response = {'error' : 'Email not found'}
        finally:
            return self.create_response(request, response)
    
    
    
"""
Why two resources for participant? See here:
http://stackoverflow.com/questions/10693379/can-django-tastypie-display-a-different-set-of-fields-in-the-list-and-detail-view
""" 
class ParticipantFullResource(ModelResource):
    contact = fields.ToOneField('server.api.ContactResource', 'contact', full=True)
    contacts = fields.ToManyField('server.api.ContactResource', 'contacts', full=True)
    class Meta:
        include_resource_uri = False
        queryset = Participant.objects.all()
        authorization= Authorization()
        paginator_class = Paginator
        serializer = PrettyJSONSerializer()
        authentication = CustomAuthentication()
        authorization = DjangoAuthorization()
        list_allowed_methods = ['get']
        """authentication = OAuth20Authentication()"""
        resource_name = 'participant_full'
        filtering = {
            'name': ALL,
            'username': ALL,
        }
        
    def prepend_urls(self):
        return [
               url(r"^(?P<resource_name>%s)/edit_profile/$" % self._meta.resource_name,
                   self.wrap_view('edit_profile'), name="edit_profile"),
                url(r"^(?P<resource_name>%s)/checkin_validate/$" % self._meta.resource_name,
                   self.wrap_view('checkin_validate'), name="checkin_validate"),
           ]
    
    def get_object_list(self, request):
        list = super(ParticipantFullResource, self).get_object_list(request)
        if request.method == "GET":
            
            params = request.GET
            uname = params['user_name']
            print "Username: %s" % uname
            user = User.objects.get(username__exact=uname)
            if user.is_superuser :
                return list
            else :
                return list.filter(username=uname)
        return []
        
        #user.is_superuser
        
    def checkin_validate(self, request, **kwargs):
        self.method_check(request, allowed=['get'])
        self.throttle_check(request)
        try:
            params = request.GET
            p = Participant.objects.get(hash = params['hash'])
            response = {'participant' : 'valid'}
        except Participant.DoesNotExist:
            response = {'participant' : 'invalid'}
        except:
            response = {'error' : 'missing params'}
        finally:
            return self.create_response(request, response)
        
    def edit_profile(self, request, **kwargs):
        self.method_check(request, allowed=['post'])
        self.is_authenticated(request)
        self.throttle_check(request)
        r = json.loads(request.body)
        p = Participant.objects.get(username = r.get('user_name'))
        linkedin_url = r.get('linkedin_url')
        facebook_url = r.get('facebook_url')
        if linkedin_url is not None:
            p.linkedin_url = linkedin_url
        if facebook_url is not None:
            p.facebook_url = facebook_url
        if linkedin_url is None and facebook_url is None:
            response = {'error' : 'No data to edit'}
        else:
            p.save()
            response = {'data' : 'edited'}
        return self.create_response(request, response)        
        
class ParticipantResource(ModelResource):
    class Meta:
        include_resource_uri = False
        queryset = Participant.objects.all()
        authorization= Authorization()
        paginator_class = Paginator
        serializer = PrettyJSONSerializer()
        authentication = CustomAuthentication()
        authorization = DjangoAuthorization()
        excludes = ['qrcode']
        list_allowed_methods = ['get']
        """authentication = OAuth20Authentication()"""
        filtering = {
            'name': ALL,
            'username': ALL,
            'last_modified_date' :['gt']
        }
        
    #The participant can only see details of participants that it are his contacts
    #The participant who makes the request won't receive is details
    def get_object_list(self, request):
        if request.method == "GET":
            
            params = request.GET
            print params
            uname = params['user_name']
            print "Username: %s" % uname
            
            my_part = Participant.objects.get(username=uname)
            
            result = Participant.objects.filter(contact__in= my_part.contacts.all())
            return result
        return []
    
    def prepend_urls(self):
        return [
               url(r"^(?P<resource_name>%s)/forgot_password/$" % self._meta.resource_name,
                   self.wrap_view('forgot_password'), name="forgot_password"),
           ]
        
    def forgot_password(self, request, **kwargs):
        self.method_check(request, allowed=['post'])
        r = json.loads(request.body)
        try:
            participant_adding = Participant.objects.get(email = r.get('email'))
            
            try:
                password_reset_request = ParticipantResetPassword.objects.get(participant_sent_request = participant_adding)
            except ParticipantResetPassword.DoesNotExist:
                password_reset_request = ParticipantResetPassword()
                password_reset_request.token_id = createSaltedMD5Hash(participant_adding.username + participant_adding.email)
                password_reset_request.participant_sent_request = participant_adding
            
            print "before email"
            status = self.send_reset_password_email(participant_adding, password_reset_request, participant_adding.email)
            if status == 1:
                password_reset_request.save()
                response = {'request' : 'sent'}
            else:
                response = {'error' : 'Error while sending email'}
        except Participant.DoesNotExist:
            response = {'error' : 'Participant not found'}
        except:
            response = {'error' : 'Participant not found'}
        finally:
            return self.create_response(request, response)
        
    
            
    def send_reset_password_email(self, participant_adding, password_reset_request, email):
            return send_mail('Password Reset Request','You can reset your password in the following link: http://'+SERVER_ADDRESS+'/forgot_password/' +
                  password_reset_request.token_id+'/', 'django.app@gmail.com', [email], fail_silently=True)
        
        
class CityResource(ModelResource):
    class Meta:
        include_resource_uri = False
        queryset = City.objects.all()
        authorization= Authorization()
        paginator_class = Paginator
        serializer = PrettyJSONSerializer()
        authentication = CustomAuthentication()
        authorization = DjangoAuthorization()
        list_allowed_methods = ['get']
        """authentication = OAuth20Authentication()"""
        
        filtering = {
            'name': ALL,
        }
        
        
class SightResource(ModelResource):
    city = fields.ForeignKey(CityResource, 'city')
    class Meta:
        include_resource_uri = False
        queryset = Sight.objects.select_subclasses()
        authorization= Authorization()
        paginator_class = Paginator
        authentication = CustomAuthentication()
        authorization = DjangoAuthorization()
        list_allowed_methods = ['get']
        """authentication = OAuth20Authentication()"""
        
    def dehydrate(self, bundle):
        # bundle.data['custom_field'] = "Whatever you want"
        if isinstance(bundle.obj, Hotel):
            hotel_res = HotelResource()
            rr_bundle = hotel_res.build_bundle(obj=bundle.obj, request=bundle.request)
            bundle.data = hotel_res.full_dehydrate(rr_bundle).data
        elif isinstance(bundle.obj, Restaurant):
            restaurant_res = RestaurantResource()
            br_bundle = restaurant_res.build_bundle(obj=bundle.obj, request=bundle.request)
            bundle.data = restaurant_res.full_dehydrate(br_bundle).data
        return bundle
        
class RestaurantResource(ModelResource):
    city = fields.ForeignKey(CityResource, 'city')
    class Meta:
        include_resource_uri = False
        queryset = Restaurant.objects.all()
        authorization= Authorization()
        paginator_class = Paginator
        authentication = CustomAuthentication()
        authorization = DjangoAuthorization()
        list_allowed_methods = ['get']
        """authentication = OAuth20Authentication()"""
        
        filtering = {
            'name': ALL,
        } 
        
class HotelResource(ModelResource):
    city = fields.ForeignKey(CityResource, 'city')
    class Meta:
        include_resource_uri = False
        queryset = Hotel.objects.all()
        authorization= Authorization()
        paginator_class = Paginator
        authentication = CustomAuthentication()
        authorization = DjangoAuthorization()
        list_allowed_methods = ['get']
        """authentication = OAuth20Authentication()"""
        
        filtering = {
            'name': ALL,
        } 
        
class ConferenceResource(ModelResource):
    city = fields.ForeignKey(CityResource, 'city')
    class Meta:
        include_resource_uri = False
        queryset = Conference.objects.all()
        authorization= Authorization()
        serializer = PrettyJSONSerializer()
        authentication = CustomAuthentication()
        #authorization = DjangoAuthorization()
        list_allowed_methods = ['get']
        """authentication = OAuth20Authentication()"""
        
        filtering = {
            'name': ALL,
        }
        
class EventResource(ModelResource):
    class Meta:
        include_resource_uri = False
        queryset = Event.objects.select_subclasses()
        authorization= Authorization()
        paginator_class = Paginator
        authentication = CustomAuthentication()
        authorization = DjangoAuthorization()
        list_allowed_methods = ['get']
        """authentication = OAuth20Authentication()"""
        
        serializer = PrettyJSONSerializer()
        filtering = {
            'title':ALL,
            'time' :('exact', 'startswith',),
            'last_modified_date' :['gt']
        }
        
    def dehydrate(self, bundle):
        # bundle.data['custom_field'] = "Whatever you want"
        if isinstance(bundle.obj, TalkSession):
            talksession_res = TalkSessionResource()
            rr_bundle = talksession_res.build_bundle(obj=bundle.obj, request=bundle.request)
            bundle.data = talksession_res.full_dehydrate(rr_bundle).data
        elif isinstance(bundle.obj, PosterSession):
            poster_res = PosterSessionResource()
            br_bundle = poster_res.build_bundle(obj=bundle.obj, request=bundle.request)
            bundle.data = poster_res.full_dehydrate(br_bundle).data
        elif isinstance(bundle.obj, KeynoteSession):
            keynote_res = KeynoteSessionResource()
            br_bundle = keynote_res.build_bundle(obj=bundle.obj, request=bundle.request)
            bundle.data = keynote_res.full_dehydrate(br_bundle).data
        elif isinstance(bundle.obj, WorkshopSession):
            workshop_res = WorkshopSessionResource()
            br_bundle = workshop_res.build_bundle(obj=bundle.obj, request=bundle.request)
            bundle.data = workshop_res.full_dehydrate(br_bundle).data
        return bundle
        
class TalkSessionResource(ModelResource):
    articles = fields.ToManyField('server.api.ArticleResource', 'articles', full=True)
    conference= fields.ForeignKey('server.api.ConferenceResource', 'conference', full=True)
    class Meta:
        include_resource_uri = False
        queryset = TalkSession.objects.all()
        authorization= Authorization()
        paginator_class = Paginator
        serializer = PrettyJSONSerializer()
        authentication = CustomAuthentication()
        authorization = DjangoAuthorization()
        list_allowed_methods = ['get']
        """authentication = OAuth20Authentication()"""
        filtering = {
            'title':ALL,
            'time' :('exact', 'startswith',),
            'last_modified_date' :['gt']
        }

class PosterSessionResource(ModelResource):
    posters = fields.ToManyField('server.api.PosterResource', 'posters', full=True)
    conference= fields.ForeignKey('server.api.ConferenceResource', 'conference', full=True)
    class Meta:
        include_resource_uri = False
        queryset = PosterSession.objects.all()
        authorization= Authorization()
        paginator_class = Paginator
        serializer = PrettyJSONSerializer()
        authentication = CustomAuthentication()
        authorization = DjangoAuthorization()
        list_allowed_methods = ['get']
        """authentication = OAuth20Authentication()"""
        filtering = {
            'title':ALL,
            'time' :('exact', 'startswith',),
            'last_modified_date' :['gt']
        }
        
        

class KeynoteSessionResource(ModelResource):
    keynotes = fields.ToManyField('server.api.KeynoteSpeakerResource', 'keynotes', full=True)
    conference= fields.ForeignKey('server.api.ConferenceResource', 'conference', full=True)
    class Meta:
        include_resource_uri = False
        queryset = KeynoteSession.objects.all()
        authorization= Authorization()
        paginator_class = Paginator
        resource_name = 'keynotesession'
        serializer = PrettyJSONSerializer()
        authentication = CustomAuthentication()
        authorization = DjangoAuthorization()
        list_allowed_methods = ['get']
        """authentication = OAuth20Authentication()"""
        filtering = {
            'title':ALL,
            'time' :('exact', 'startswith',),
            'last_modified_date' :['gt']
        }
        
class WorkshopSessionResource(ModelResource):
    conference= fields.ForeignKey('server.api.ConferenceResource', 'conference', full=True)
    class Meta:
        include_resource_uri = False
        queryset = WorkshopSession.objects.all()
        authorization= Authorization()
        paginator_class = Paginator
        resource_name = 'workshop'
        serializer = PrettyJSONSerializer()
        authentication = CustomAuthentication()
        authorization = DjangoAuthorization()
        list_allowed_methods = ['get']
        """authentication = OAuth20Authentication()"""
        filtering = {
            'title':ALL,
            'time' :('exact', 'startswith',),
            'last_modified_date' :['gt']
        }
        
class SocialEventResource(ModelResource):
    class Meta:
        include_resource_uri = False
        queryset = SocialEvent.objects.all()
        authorization= Authorization()
        paginator_class = Paginator
        resource_name = 'socialevent'
        serializer = PrettyJSONSerializer()
        authentication = CustomAuthentication()
        authorization = DjangoAuthorization()
        list_allowed_methods = ['get']
        """authentication = OAuth20Authentication()"""
        filtering = {
            'title':ALL,
            'time' :('exact', 'startswith',),
            'last_modified_date' :['gt']
        }
        
class AuthorResource(ModelResource):
    contact = fields.ToOneField('server.api.ContactResource', 'contact', full=True,null=True)
    participant = fields.ToOneField('server.api.ParticipantResource', 'participant', full=True,null=True)

    class Meta:
        include_resource_uri = False
        queryset = Author.objects.all()
        authorization= Authorization()
        paginator_class = Paginator
        serializer = PrettyJSONSerializer()
        authentication = CustomAuthentication()
        authorization = DjangoAuthorization()
        list_allowed_methods = ['get']
        """authentication = OAuth20Authentication()"""
        
        filtering = {
            'name': ALL,
            'email':ALL,
        }
        
class PublicationResource(ModelResource):
    class Meta:
        include_resource_uri = False
        queryset = Publication.objects.all()
        authorization= Authorization()
        paginator_class = Paginator
        serializer = PrettyJSONSerializer()
        authentication = CustomAuthentication()
        authorization = DjangoAuthorization()
        list_allowed_methods = ['get']
        """authentication = OAuth20Authentication()"""
        
        filtering = {
            'title': ALL,
        }
              
class ArticleResource(ModelResource):
    authors = fields.ToManyField('server.api.AuthorResource', 'authors', full=True)
    class Meta:
        include_resource_uri = False
        queryset = Article.objects.all()
        authorization= Authorization()
        paginator_class = Paginator
        serializer = PrettyJSONSerializer()
        authentication = CustomAuthentication()
        authorization = DjangoAuthorization()
        list_allowed_methods = ['get']
        """authentication = OAuth20Authentication()"""
        
        filtering = {
            'title': ALL,
        }

class NotPresentedPublicationResource(ModelResource):
    class Meta:
        include_resource_uri = False
        queryset = NotPresentedPublication.objects.all()
        authorization= Authorization()
        paginator_class = Paginator
        serializer = PrettyJSONSerializer()
        authentication = CustomAuthentication()
        authorization = DjangoAuthorization()
        list_allowed_methods = ['get']
        """authentication = OAuth20Authentication()"""
        
        filtering = {
            'title': ALL,
        }

class PosterResource(ModelResource):
    authors = fields.ToManyField('server.api.AuthorResource', 'authors', full=True)
    class Meta:
        include_resource_uri = False
        queryset = Poster.objects.all()
        authorization= Authorization()
        serializer = PrettyJSONSerializer()
        authentication = CustomAuthentication()
        authorization = DjangoAuthorization()
        list_allowed_methods = ['get']
        """authentication = OAuth20Authentication()"""
        
        paginator_class = Paginator
        filtering = {
            'title' : ALL,
        }

class OrganizationMemberResource(ModelResource):
    conference = fields.ForeignKey(ConferenceResource, 'conference')
    class Meta:
        include_resource_uri = False
        queryset = OrganizationMember.objects.all()
        authorization= Authorization()
        serializer = PrettyJSONSerializer()
        authentication = CustomAuthentication()
        authorization = DjangoAuthorization()
        list_allowed_methods = ['get']
        """authentication = OAuth20Authentication()"""
        
        filtering = {
            'name': ALL,
            'email':ALL,
        }    

class NotificationResource(ModelResource):
    class Meta:
        include_resource_uri = False
        queryset = Notification.objects.all()
        authorization= Authorization()
        serializer = PrettyJSONSerializer()
        authentication = CustomAuthentication()
        authorization = DjangoAuthorization()
        list_allowed_methods = ['get']
        """authentication = OAuth20Authentication()"""
        filtering = {
            'title' : ALL,
            'date' :('exact', 'startswith', 'gt'),
        }
        

class KeynoteSpeakerResource(ModelResource):
    contact = fields.ToOneField('server.api.ContactResource', 'contact', full=True,null=True)
    participant = fields.ToOneField('server.api.ParticipantResource', 'participant', full=True,null=True)
    class Meta:
        include_resource_uri = False
        queryset = KeynoteSpeaker.objects.all()
        authorization= Authorization()
        paginator_class = Paginator
        serializer = PrettyJSONSerializer()
        authentication = CustomAuthentication()
        authorization = DjangoAuthorization()
        list_allowed_methods = ['get']
        """authentication = OAuth20Authentication()"""
        
        filtering = {
            'name': ALL,
            'email': ALL,
        }     

class SponsorResource(ModelResource):
    class Meta:
        include_resource_uri = False
        queryset = Sponsor.objects.all()
        authorization= Authorization()
        serializer = PrettyJSONSerializer()
        authentication = CustomAuthentication()
        authorization = DjangoAuthorization()
        list_allowed_methods = ['get']
        """authentication = OAuth20Authentication()"""

class PinResource(Resource):
    class Meta:
        resource_name = 'pin'
        allowed_methods = ['post']
        authentication = CustomAuthentication()
        authorization = Authorization()
        serializer = PrettyJSONSerializer()
    
    def prepend_urls(self):
        return [
            url(r"^(?P<resource_name>%s)/generate/$" % self._meta.resource_name,
                self.wrap_view('generate_pin'), name="generate_pin"),
            url(r"^(?P<resource_name>%s)/remove/$" % self._meta.resource_name,
                self.wrap_view('remove_pin'), name="remove_pin"),
        ]
        
    def generate_pin(self, request, **kwargs):
        self.method_check(request, allowed=['post'])
        self.is_authenticated(request)
        self.throttle_check(request)
        r = json.loads(request.body)
        exchange_pin = ExchangePIN()
        exchange_pin.owner = Participant.objects.get(username = r.get('user_name'))
        exchange_pin.save()
        response = {"pin" : str(exchange_pin.pin).zfill(4) }
        
        return self.create_response(request, response)
    
    def remove_pin(self, request, **kwargs):
        self.method_check(request, allowed=['post'])
        self.is_authenticated(request)
        self.throttle_check(request)
        r = json.loads(request.body)
        try:
            exchange_pin = ExchangePIN.objects.get(owner_id = r.get('user_name'))
            exchange_pin.delete()
            response = {"pin" : "removed"}
        except ExchangePIN.DoesNotExist:
            response = {"pin" : "not found"}
            return self.create_response(request, response, response_class=HttpNotFound)
        return self.create_response(request, response)
