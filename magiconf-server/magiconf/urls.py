from django.conf.urls import patterns, include, url
from tastypie.api import Api
from server.api import ParticipantResource,ParticipantFullResource,ContactResource, SocialEventResource,CityResource, SightResource, RestaurantResource, HotelResource, ConferenceResource, EventResource, TalkSessionResource, AuthorResource, PosterSessionResource,PublicationResource,PosterResource,KeynoteSessionResource,WorkshopSessionResource,SponsorResource,KeynoteSpeakerResource,OrganizationMemberResource,NotificationResource,ArticleResource,NotPresentedPublicationResource,PinResource
from django.contrib import admin
from django.conf import settings
from django.views.generic import TemplateView


admin.autodiscover()

v1_api = Api(api_name='v1')
v1_api.register(ParticipantResource())
v1_api.register(ParticipantFullResource())
v1_api.register(AuthorResource())
v1_api.register(PosterSessionResource())
v1_api.register(KeynoteSessionResource())
v1_api.register(PublicationResource())

v1_api.register(ArticleResource())
v1_api.register(NotPresentedPublicationResource())
v1_api.register(PosterResource())

v1_api.register(OrganizationMemberResource())
v1_api.register(NotificationResource())
v1_api.register(KeynoteSpeakerResource())
v1_api.register(SponsorResource())

v1_api.register(ContactResource())
v1_api.register(CityResource())
v1_api.register(SightResource())
v1_api.register(RestaurantResource())
v1_api.register(HotelResource())
v1_api.register(ConferenceResource())
v1_api.register(EventResource())
v1_api.register(TalkSessionResource())
v1_api.register(WorkshopSessionResource())
v1_api.register(SocialEventResource())
v1_api.register(PinResource())



urlpatterns = patterns('',
    # Examples:
    # url(r'^$', 'magiconf.views.home', name='home'),
    # url(r'^magiconf/', include('magiconf.foo.urls')),

    # Uncomment the admin/doc line below to enable admin documentation:
    # url(r'^admin/doc/', include('django.contrib.admindocs.urls')),

    # Uncomment the next line to enable the admin:
    
    url(r'^admin/', include(admin.site.urls)),
    (r'^api/', include(v1_api.urls)),
    url(r'^validate_user/(?P<username>\w+)/(?P<password>\w+)$', 'server.views.validade_user'),
        url(r'', include('gcm.urls')),
        url(r'^confirm_contact_exchange/(?P<token>\w+)/$','server.views.confirm_contact_exchange'),
        url(r'^forgot_password/(?P<token>\w+)/$','server.views.forgot_password'),
        url(r'^password_reset_done/$', TemplateView.as_view(template_name='password_reset_done.html'), name="password_reset_done"),
       # (r'^password_reset_done/$',direct_to_template, {'template': 'password_reset_done.html'}),
)

if settings.DEBUG:
    # static files (images, css, javascript, etc.)
    urlpatterns += patterns('',
        (r'^photos/(?P<path>.*)$', 'django.views.static.serve', {
        'document_root': settings.MEDIA_ROOT}))
