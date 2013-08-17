from django.contrib import admin
from server.models import CustomUser,Participant,Contact, City, Sight, Restaurant, Hotel,SocialEvent, Conference, TalkSession, Author, Event, PosterSession,KeynoteSession,WorkshopSession,Article,OrganizationMember,Poster,Notification,NotPresentedPublication,KeynoteSpeaker,Sponsor
from django.contrib.auth.admin import UserAdmin
from django.contrib.auth.models import User


# Define an inline admin descriptor for Employee model
# which acts a bit like a singleton
class ParticipantInline(admin.StackedInline):
    model = Participant
    can_delete = False
    verbose_name_plural = 'participant'
    exclude = ('contacts','contact','qrcode','last_modified_date',)
    #readonly_fields=('username','photo','work_place','hash','name','country','phone_number','email','linkedin_url','facebook_url','device',)
    def get_readonly_fields(self, request, obj=None):
        if obj:
            return self.readonly_fields + ('username','photo','work_place','hash','name','country','phone_number','email','linkedin_url','facebook_url','device',)
        return []
    

# Define a new User admin
class MyUserAdmin(UserAdmin):
    inlines = (ParticipantInline, )
    
    change_form_template = 'change_form.html'
    
    def get_readonly_fields(self, request, obj=None):
        if obj:
            return self.readonly_fields + ('username',)
        return []
    
# Re-register UserAdmin
admin.site.unregister(User)
admin.site.register(CustomUser, MyUserAdmin)


class AuthorAdmin(admin.ModelAdmin):
    exclude = ('participant','contact',)
    
class KenoteSpeakerAdmin(admin.ModelAdmin):
    exclude = ('participant','contact',)
    
class NotificationAdmin(admin.ModelAdmin):
    exclude = ('participants',)
    
class ParticipantAdmin(admin.ModelAdmin):
    exclude = ('contacts','contact','qrcode','user','last_modified_date','hash')
    readonly_fields=('username',)
    
'''class ContactAdmin(admin.ModelAdmin):
    list_display = ['id']'''

admin.site.register(Participant, ParticipantAdmin)
#admin.site.register(Contact,ContactAdmin)
admin.site.register(City)
admin.site.register(Sight)
admin.site.register(Restaurant)
admin.site.register(Hotel)
admin.site.register(Conference)
#admin.site.register(Event)
admin.site.register(TalkSession)
admin.site.register(Author, AuthorAdmin)
admin.site.register(PosterSession)
admin.site.register(KeynoteSession)
admin.site.register(WorkshopSession)
admin.site.register(SocialEvent)
admin.site.register(Article)
admin.site.register(OrganizationMember)
admin.site.register(Poster)
admin.site.register(Notification, NotificationAdmin)
#admin.site.register(NotPresentedPublication)
admin.site.register(KeynoteSpeaker, KenoteSpeakerAdmin)
admin.site.register(Sponsor)

