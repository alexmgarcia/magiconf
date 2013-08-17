from django.db import models
from model_utils.managers import InheritanceManager
from django.contrib.contenttypes.models import ContentType
from PyQRNative import *
from server.utils.qrcodehandler import QRCodeHandler
from django.core.files import File
from django.core.files.base import ContentFile
from cStringIO import StringIO
from django.contrib.auth.models import User
from random import randint
from django.core.exceptions import ObjectDoesNotExist
from gcm.models import Device
from server.authentication import MD5Hasher
import datetime


from django.contrib.auth.models import User
from django.utils.translation import ugettext_lazy as _

class CustomUser(User):
    class Meta:
        proxy = True
        app_label = 'auth'
        verbose_name = 'Utilizador'
        verbose_name_plural = 'Utilizadores'

# Create your models here.

class Participant(models.Model):
    username = models.CharField(max_length=20, verbose_name=_("Nome de utilizador"))
    username.primary_key = True
    #password = models.CharField(max_length=128)
    work_place = models.CharField(max_length=50, verbose_name=_("Organizacao"))
    photo = models.FileField(upload_to='user_photo',blank=True,null=True, verbose_name=_("Foto"))
    name = models.CharField(max_length=30, verbose_name=_("Nome"))
    country = models.CharField(max_length=20, verbose_name=_("Pais"))
    phone_number = models.IntegerField(max_length=9, verbose_name=_("Telefone"))
    linkedin_url = models.URLField(null=True, blank=True, verbose_name=_("Linkedin"))
    facebook_url = models.URLField(null=True, blank=True, verbose_name=_("Facebook"))
    email = models.EmailField(unique = True, verbose_name=_("E-mail"))
    qrcode = models.FileField(upload_to = 'qrcodes',null=True,blank=True, verbose_name=_("Codigo QR"))
    contact = models.OneToOneField('Contact', related_name= 'participant_contact')
    user = models.OneToOneField(User)  
    contacts = models.ManyToManyField('Contact', related_name='contact_list', null=True, blank=True)
    device = models.OneToOneField(Device, null=True, blank=True, verbose_name=_("Dispositivo"))
    last_modified_date = models.DateTimeField(blank=True)
    hash = models.CharField(max_length=32, blank=True)
    
    class Meta:
        verbose_name = "Participante"
        verbose_name_plural = "Participantes"
    
    def save( self, *args, **kw ):

        self.username = self.user.username
        self.last_modified_date = datetime.datetime.now()
        try:
            self.contact == None
        except ObjectDoesNotExist:
            c= Contact()
            c.save()
            self.contact = c
            hasher = MD5Hasher()
            self.hash = hasher.encode(self.username, 'username')
        super( Participant, self ).save( *args, **kw )
    
    def __unicode__(self):
        return self.username

class ParticipantResetPassword(models.Model):
    token_id = models.CharField(max_length = 32)
    token_id.primary_key = True
    participant_sent_request = models.ForeignKey(Participant, related_name='participant_sent_reset_pw_request')
    
    class Meta:
        verbose_name = "RedefirPalavraPasseParticipante"
        verbose_name_plural = "RedefirPalavraPasseParticipantes"

class ParticipantExchangeEmailRelationship(models.Model):
    token_id = models.CharField(max_length = 32)
    token_id.primary_key = True
    participant_sent_request = models.ForeignKey(Participant, related_name='participant_sent_request')
    participant_received_request = models.ForeignKey(Participant, related_name='participant_received_request')
    
    class Meta:
        verbose_name = "RelacaoTrocaContactoMail"
        verbose_name_plural = "RelacoesTrocaContactoMail"
        
    
class ExchangePIN(models.Model):
    pin = models.IntegerField(max_length=4)
    date_created = models.DateTimeField(auto_now_add=True)
    owner = models.OneToOneField(Participant)
    
    class Meta:
        verbose_name = "TrocaPIN"
        verbose_name_plural = "TrocasPIN"
        unique_together = ('pin', 'date_created')
    
    def save(self, *args, **kw):
        try:
            while True:
                gen_pin = randint(0, 9999)
                ExchangePIN.objects.get(pin = gen_pin)
        except ExchangePIN.DoesNotExist:
            self.pin = gen_pin
            obj = ExchangePIN.objects.filter(owner = self.owner)
            if obj is not None:
                obj.delete()
            super(ExchangePIN, self).save(*args, **kw)
                
    def __unicode__(self):
        return str(self.pin) + "-" + self.owner.username + "-" + str(self.date_created)

class Contact(models.Model):
   
    id = models.AutoField(primary_key=True)
    class Meta:
        verbose_name = "Contacto"
        verbose_name_plural = "Contactos"
    
class City(models.Model):
    name = models.CharField(max_length=20, verbose_name=_("Nome"))
    name.primary_key = True
    description = models.TextField(max_length=2000, verbose_name=_("Descricao"))
    photo = models.FileField(upload_to='city',blank=True,null=True, verbose_name=_("Foto"))
    
    class Meta:
        verbose_name = "Cidade"
        verbose_name_plural = "Cidades"
    
    def __unicode__(self):
        return self.name
    
class Sight(models.Model):
    objects = InheritanceManager()
    name = models.CharField(max_length=30, verbose_name=_("Nome"))
    address = models.CharField(max_length=50, verbose_name=_("Local"))
    description = models.TextField(verbose_name=_("Descricao"))
    latitude = models.DecimalField(max_digits=15, decimal_places=10)
    longitude = models.DecimalField(max_digits=15, decimal_places=10)
    photo = models.FileField(upload_to='sights',blank=True,null=True, verbose_name=_("Foto"))
    city = models.ForeignKey(City, verbose_name=_("Cidade"))
    
    class Meta:
        unique_together = ('name', 'address')
        verbose_name = "Ponto de Interesse"
        verbose_name_plural = "Pontos de Interesse"
        
    def __unicode__(self):
        return self.name

class Hotel(Sight):
    
    STARS_CHOICES = ((5, '5') ,(4, '4'),(3, '3'),(2,'2'),(1,'1'))
    
    stars = models.IntegerField(choices=STARS_CHOICES, verbose_name=_("Classificacao"))
    phone_number = models.CharField(max_length=9, verbose_name=_("Telefone"))
    
    class Meta:
        verbose_name = "Hotel"
        verbose_name_plural = "Hot\xe9is".decode("latin-1")
    
class Restaurant(Sight):
    phone_number = models.CharField(max_length=9, verbose_name=_("Telefone"))
    
    class Meta:
        verbose_name = "Restaurante"
        verbose_name_plural = "Restaurantes"
    
class Conference(models.Model):
    name = models.CharField(max_length=100, verbose_name=_("Nome"))
    edition = models.IntegerField(verbose_name=_("Edicao"))
    place = models.CharField(max_length=100, verbose_name=_("Local"))
    beginning_date = models.DateField(verbose_name=_("Data de inicio"))
    ending_date = models.DateField(verbose_name=_("Data de fim"))
    website = models.URLField(max_length=100)
    description = models.TextField(max_length=2000, verbose_name=_("Descricao"))
    latitude = models.DecimalField(max_digits=15, decimal_places=10)
    longitude = models.DecimalField(max_digits=15, decimal_places=10)
    city = models.ForeignKey(City, verbose_name=_("Cidade"))
    photo = models.FileField(upload_to='conference_logos',null=True, blank=True, verbose_name=_("Foto"))
    
    class Meta:
        unique_together = ('name', 'edition', 'place')
        verbose_name = "Confer\xeancia".decode("latin-1")
        verbose_name_plural = "Confer\xeancias".decode("latin-1")
        
    def __unicode__(self):
        return self.name

class Event(models.Model):
    objects = InheritanceManager()
    title = models.CharField(max_length=100, verbose_name=_("Titulo"))
    place = models.CharField(max_length=50, verbose_name=_("Local"))
    time = models.DateTimeField(verbose_name=_("Data e Hora"))
    duration = models.IntegerField(verbose_name=_("Duracao (min)"))
    conference = models.ForeignKey(Conference, verbose_name=_("Conferencia"))
    last_modified_date = models.DateTimeField(blank=True, null=True, editable=False)
    
    class Meta:
        unique_together = ('title', 'time')
        verbose_name = "Evento"
        verbose_name_plural = "Eventos"
        
    def save( self, *args, **kw ):
        self.last_modified_date = datetime.datetime.now()
        super( Event, self ).save( *args, **kw )
        
    def __unicode__(self):
        return self.title
 
class TalkSession(Event):
    articles = models.ManyToManyField('Article', verbose_name=_("Artigos"))
    
    class Meta:
        verbose_name = "Sess\xe3o Talk".decode("latin-1")
        verbose_name_plural = "Sess\xf5es Talk".decode("latin-1")



class PosterSession(Event):
    posters = models.ManyToManyField('Poster')
    class Meta:
        verbose_name = "Sess\xe3o de Posters".decode("latin-1")
        verbose_name_plural = "Sess\xf5es de Posters".decode("latin-1")
    
class KeynoteSession(Event):
    keynotes = models.ManyToManyField('KeynoteSpeaker', verbose_name=_("Oradores Keynote"))
    description = models.TextField(max_length=1500, verbose_name=_("Descricao"))
    
    class Meta:
        verbose_name = "Sess\xe3o Keynote".decode("latin-1")
        verbose_name_plural = "Sess\xf5es Keynote".decode("latin-1")
    
class WorkshopSession(Event):
    description = models.TextField(max_length=1500, verbose_name=_("Descricao"))
    
    class Meta:
        verbose_name = "Workshop"
        verbose_name_plural = "Workshops"
    
class SocialEvent(Event):
    dumpField = models.BooleanField(default=True, editable=False)
    
    class Meta:
        verbose_name = "Evento Social"
        verbose_name_plural = "Eventos Sociais"
    
    
class Publication(models.Model):
    objects = InheritanceManager()
    title = models.CharField(max_length=120, verbose_name=_("Titulo"))
    title.primary_key = True
    abstract = models.TextField(max_length=1000, verbose_name=_("Resumo"))
    def __unicode__(self):
        return self.title
    
    class Meta:
        verbose_name = "Publica\xe7\xe3o".decode("latin-1")
        verbose_name_plural = "Publica\xe7\xf5es".decode("latin-1")

class Article(Publication):
    authors = models.ManyToManyField('Author', verbose_name=_("Autores"))
    
    class Meta:
        verbose_name = "Artigo"
        verbose_name_plural = "Artigos"
    
class NotPresentedPublication(Publication):
    authors = models.TextField(max_length = 200, verbose_name=_("Autores"))
    
    class Meta:
        verbose_name = "Publica\xe7\xe3o n\xe3o Apresentada".decode("latin-1")
        verbose_name_plural = "Publica\xe7\xf5es n\xe3o Apresentadas".decode("latin-1")
    
class Poster(models.Model):
    title = models.CharField(max_length=120, verbose_name=_("Titulo"))
    title.primary_key = True
    authors = models.ManyToManyField('Author', verbose_name=_("Autores"))
    
    def __unicode__(self):
        return self.title
    
    class Meta:
        verbose_name = "Poster"
        verbose_name_plural = "Posters"
     
class OrganizationMember(models.Model):
    email = models.EmailField(verbose_name=_("E-mail"))
    email.primary_key = True
    name = models.CharField(max_length=20, verbose_name=_("Nome"))
    photo = models.FileField(upload_to='org_member',blank=True,null=True, verbose_name=_("Foto"))
    
    ROLE_CHOICES = (
        ('CC', 'Comissao Coordenadora'),
        ('CO', 'Comissao Organizadora'),
        ('CP', 'Comissao Programa'),
    )
    role = models.CharField(max_length=2, choices=ROLE_CHOICES, verbose_name=_("Funcao"))
    
    work_place = models.CharField(max_length=50, verbose_name=_("Organizacao"))
    country = models.CharField(max_length=20, verbose_name=_("Pais"))
    conference = models.ForeignKey(Conference, verbose_name=_("Conferencia"))
    
    def __unicode__(self) :
        return self.name
    
    class Meta:
        verbose_name = "Membro da Organiza\xe7\xe3o".decode("latin-1")
        verbose_name_plural = "Membros da Organiza\xe7\xe3o".decode("latin-1")

class Notification(models.Model):
    title = models.CharField(max_length=20, verbose_name=_("Titulo"))
    date = models.DateTimeField(blank=True, editable=False)
    description = models.TextField(max_length=100, verbose_name=_("Descricao"))
    participants = models.ManyToManyField(Participant)
    
    class Meta:
        unique_together = ('title', 'date')
        verbose_name = "Notifica\xe7\xe3o".decode("latin-1")
        verbose_name_plural = "Notifica\xe7\xf5es".decode("latin-1")
        
    def save( self, *args, **kw ):
        self.date = datetime.datetime.now()
        devices = Device.objects.filter(is_active=True)
        for device in devices:
            device.send_message("Title:" + self.title + "|Description:" + self.description)
        super( Notification, self ).save( *args, **kw )
        
    def __unicode__(self):
        return self.title + ' ' + str(self.date)
        
class Author(models.Model):
    email = models.EmailField(verbose_name=_("E-mail"))
    email.primary_key = True
    name = models.CharField(max_length=30, verbose_name=_("Nome"))
    work_place = models.CharField(max_length=50, verbose_name=_("Organizacao"))
    country = models.CharField(max_length=20, verbose_name=_("Pais"))
    photo = models.FileField(upload_to='authors',blank=True,null=True, verbose_name=_("Foto"))
    participant = models.OneToOneField(Participant, null=True, blank=True)
    contact = models.OneToOneField(Contact,related_name = 'author_contact', null=True, blank=True)
    
    def save( self, *args, **kw ):
        c= Contact()
        c.save()
        self.contact = c
        try:
            p = Participant.objects.get(email= self.email)
        except Participant.DoesNotExist:
            p = None
        if p != None:
            self.participant = p
            print "Author is participant with username: %s" % p.username
        super( Author, self ).save( *args, **kw )
        
    def __unicode__(self):
        return self.name
    
    class Meta:
        verbose_name = "Autor"
        verbose_name_plural = "Autores"
    
class KeynoteSpeaker(models.Model):
    email = models.EmailField()
    email.primary_key = True
    name = models.CharField(max_length=20, verbose_name=_("Nome"))
    work_place = models.CharField(max_length=50, verbose_name=_("Organizacao"))
    country = models.CharField(max_length=20, verbose_name=_("Pais"))
    #photo = models.FileField(upload_to='keynote_speakers')
    photo = models.CharField(max_length=100,blank=True,null=True, verbose_name=_("Foto"))
    participant = models.OneToOneField(Participant, null=True, blank=True)
    author = models.OneToOneField(Author, null=True, blank=True, editable=False)
    contact = models.OneToOneField(Contact,related_name = 'keynote_speaker_contact', null=True, blank=True)
    
    def save( self, *args, **kw ):
        c= Contact()
        c.save()
        self.contact = c
        try:
            p = Participant.objects.get(email=self.email)
        except Participant.DoesNotExist:
            p = None
        if p != None:
            self.participant = p
            print "Keynote is participant with username: %s" % p.username 
        super( KeynoteSpeaker, self ).save( *args, **kw )
    
    def __unicode__(self):
        return self.name
    
    class Meta:
        verbose_name = "Orador Keynote"
        verbose_name_plural = "Oradores Keynote"

class Sponsor(models.Model):
    name = models.CharField(max_length=100, verbose_name=_("Nome"))
    name.primary_key = True
    logo = models.FileField(upload_to='sponsors', verbose_name=_("Logotipo"))
    website = models.URLField()
    conference = models.ForeignKey(Conference, verbose_name=_("Conferencia"))
    
    def __unicode__(self):
        return self.name
    
    class Meta:
        verbose_name = "Patrocinador"
        verbose_name_plural = "Patrocinadores"
    


#Signals

def partipantqrcode_pre_save(sender, instance, **kwargs):
    #Create a qrcode only if we are creating a new participant
    if not instance.pk:
        instance._QRCODE = True
    else:
        if hasattr(instance, '_QRCODE'):
            instance._QRCODE = False
        else:
            instance._QRCODE = True
 
models.signals.pre_save.connect(partipantqrcode_pre_save, sender=Participant, dispatch_uid="participant-creation-pre-signal")
 
def partipantqrcode_post_save(sender, instance, **kwargs):
    if instance._QRCODE:
        instance._QRCODE = False
        if not instance.qrcode:
            qr = QRCodeHandler(instance.username+'\n'+instance.hash)
            qr.buildQRCode()
            image = qr.getImage()
            #Save image to string buffer
            image_buffer = StringIO()
            image.save(image_buffer, format='JPEG')
            image_buffer.seek(0)
            file_name = 'QR_%s.jpg' % (instance.username)
            file_object = File(image_buffer, file_name)
            content_file = ContentFile(file_object.read())
            instance.qrcode.save(file_name, content_file, save=True)
 
models.signals.post_save.connect(partipantqrcode_post_save, sender=Participant, dispatch_uid="participant-creation-post-signal")


def talksessionUpdate_post_save(sender, instance, **kwargs):
    
    devices = Device.objects.filter(is_active=True)
    for device in devices:
        device.send_message("[EVENT_UPDATE]|Type:talksession" +
                            "|djangoid:" + str(instance.pk))
    print "[EVENT_UPDATE]|Type:talksession"+ "|djangoid:" + str(instance.pk)
 
models.signals.post_save.connect(talksessionUpdate_post_save, sender=TalkSession, dispatch_uid="talksession-notification-post-signal")

def workshopSessionUpdate_post_save(sender, instance, **kwargs):
    print "ENTROU AQUI"
    
    devices = Device.objects.filter(is_active=True)
    for device in devices:
        device.send_message("[EVENT_UPDATE]|Type:workshopsession" +
                            "|djangoid:" + str(instance.pk))
    print "[EVENT_UPDATE]|Type:workshopsession"+ "|djangoid:" + str(instance.pk)
 
models.signals.post_save.connect(workshopSessionUpdate_post_save, sender=WorkshopSession, dispatch_uid="workshopsession-notification-post-signal")

def keynoteSessionUpdate_post_save(sender, instance, **kwargs):
    
    devices = Device.objects.filter(is_active=True)
    for device in devices:
        device.send_message("[EVENT_UPDATE]|Type:keynotesession" +
                            "|djangoid:" + str(instance.pk))
    print "[EVENT_UPDATE]|Type:keynotesession"+ "|djangoid:" + str(instance.pk)
 
models.signals.post_save.connect(keynoteSessionUpdate_post_save, sender=KeynoteSession, dispatch_uid="keynotesession-notification-post-signal")

def posterSessionUpdate_post_save(sender, instance, **kwargs):
    
    devices = Device.objects.filter(is_active=True)
    for device in devices:
        device.send_message("[EVENT_UPDATE]|Type:postersession" +
                            "|djangoid:" + str(instance.pk))
    print "[EVENT_UPDATE]|Type:postersession"+ "|djangoid:" + str(instance.pk)
 
models.signals.post_save.connect(posterSessionUpdate_post_save, sender=PosterSession, dispatch_uid="postersession-notification-post-signal")

def socialEventUpdate_post_save(sender, instance, **kwargs):
    devices = Device.objects.filter(is_active=True)
    for device in devices:
        device.send_message("[EVENT_UPDATE]|Type:socialevent" +
                            "|djangoid:" + str(instance.pk))
    print "[EVENT_UPDATE]|Type:socialevent"+ "|djangoid:" + str(instance.pk)
 
models.signals.post_save.connect(socialEventUpdate_post_save, sender=SocialEvent, dispatch_uid="socialevent-notification-post-signal")





"""
def update_user_participant_pre_save(sender, instance, **kwargs):
    if instance.pk:
        instance._PART_USERNAME = instance.username

models.signals.pre_save.connect(update_user_participant_pre_save, sender=User, dispatch_uid="participant-user-update-pre-signal")

def update_user_participant_post_save(sender, instance, **kwargs):
    if instance.pk:
        print "Old username %s" %instance._PART_USERNAME
        try:
            p = Participant.objects.get(user = instance)
            print "Participant old username: %s" % p.username
            
            p.username=instance.username
            #p.user = instance
            p.save(update_fields=['username'])
        except ObjectDoesNotExist:
            print "Participant doesn't exist! oO"
        

models.signals.post_save.connect(update_user_participant_post_save, sender=User, dispatch_uid="participant-user-update-post-signal")
"""

"""
def create_user_profile(sender, instance, created, **kwargs):  
    if created:
        print dir(instance)
        print "ATE AQUI"
        try:
            #if instance.participant_user != None :
            #    print "ATE AQUI TAMBEM"
            #instance.participant_user.user = instance
            #instance.participant_user.save()
            #    p = instance.participant_user
            #    if p.user != None:
            #        print p.user
                if created:
                    profile = Participant()
                    profile.username=instance.username
                    profile.country = p.country
                    profile.work_place = p.work_place
                    profile.phone_number = p.phone_number
                    profile.email = p.email
                    profile.setUser(sender)
                    profile.save()""
                
                
                profile, created = Participant.objects.get_or_create(
                            user=instance,
                            username=instance.username,
                            country = p.country,
                            phone_number = p.phone_number,
                            email  = p.email)
                if not created:
                    profile.user = instance
                    user.save()
                    
                    
        except ObjectDoesNotExist:
            print "ignoring participant"
 
models.signals.post_save.connect(create_user_profile, sender=User, dispatch_uid="users-profilecreation-signal")"""