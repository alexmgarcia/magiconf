# Create your views here.

from django.contrib.auth import authenticate
from django.http import HttpResponse
from server.models import ExchangePIN, Participant,User,ParticipantExchangeEmailRelationship,Contact,ParticipantResetPassword
from datetime import timedelta
import json
from server.authentication import CustomAuthentication
from django.shortcuts import render
from django import forms
from django.views.decorators.cache import never_cache
from django.views.decorators.csrf import csrf_protect
from server.forms import ResetPasswordForm
from django.http import HttpResponseRedirect
from magiconf.settings import SERVER_ADDRESS
import smtplib
from django.core.mail import send_mail

'''
Validates a given login and password
'''    
def validade_user(request, username, password):
    authorizer = CustomAuthentication()
    if authorizer.validate_user(request,username,password):
        return HttpResponse('OK', status=200)
    else:
        return HttpResponse('Unauthorized', status=401)
        
    
  
def confirm_contact_exchange(request,token):
    print token
    try :
        
        exchange = ParticipantExchangeEmailRelationship.objects.get(token_id = token)
        participant1 = exchange.participant_sent_request
        participant2 = exchange.participant_received_request
        
        
        print participant1
        print participant2
        p1_device = participant1.device
        p2_device = participant2.device
        
        #Launches exception Contact.DoesNotExist
        try:    
            participant1.contacts.get(id = participant2.contact.id) != None
            context = {'status': -2, 'participant_received':participant2.name}
            return render(request, 'exchangeconfirmation.html', context)
        except Contact.DoesNotExist:
            print ""
        
        
        participant1.contacts.add(participant2.contact)
        participant1.save()
        participant1_adding_response = {'contact': 
                            {'username' : participant2.username,
                           'name' : participant2.name,
                           'work_place' : participant2.work_place,
                           'photo' : (participant2.photo.url if participant2.photo else ''),
                           'country' : participant2.country,
                           'phone_number' : participant2.phone_number,
                           'email' : participant2.email,
                           'facebook_url' : participant2.facebook_url,
                           'linkedin_url' : participant2.linkedin_url                           
                           } 
                        }
        if p1_device != None:
            p1_device.send_message("[CONTACT_EXCHANGE_DATA]" + json.dumps(participant1_adding_response))
        try:
            participant2.contacts.get(id = participant1.contact.id) != None
        except:
            print "part 2 nao tem o part 1"
            participant2.contacts.add(participant2.contact)
            participant2.save()
            participant2_adding_response = {'contact': 
                            {'username' : participant1.username,
                           'name' : participant1.name,
                           'work_place' : participant1.work_place,
                           'photo' : (participant1.photo.url if participant1.photo else ''),
                           'country' : participant1.country,
                           'phone_number' : participant1.phone_number,
                           'email' : participant1.email,
                           'facebook_url' : participant1.facebook_url,
                           'linkedin_url' : participant1.linkedin_url                           
                           } 
                        }
            print participant1_adding_response
            print participant2_adding_response
            send_mail('Detalhes de contacto', 'Nome: ' + participant1.name + '\n' +
                      u'Organiza\xe7\xe3o: ' + participant1.work_place + '\n' +
                      (('Foto: http://' + SERVER_ADDRESS + participant1.photo.url + '\n') if participant1.photo else '') +
                      u'Pa\xeds: ' + participant1.country + '\n' +
                      'Telefone: ' + str(participant1.phone_number) + '\n'
                      'E-mail: ' + participant1.email + '\n' +
                      ('Facebook: ' + participant1.facebook_url + '\n') if participant1.facebook_url != '' else '' +
                      ('Linkedin: ' + participant1.linkedin_url + '\n') if participant1.linkedin_url != '' else '', 'magiconf.app@gmail.com', [participant2.email], fail_silently=True)
        if p2_device != None:    
            p2_device.send_message("[CONTACT_EXCHANGE_DATA]" + json.dumps(participant2_adding_response))
       
        
        context = {'status': 0}
        exchange.delete()
    except ParticipantExchangeEmailRelationship.DoesNotExist:
        context = {'status': -1}
    
    return render(request, 'exchangeconfirmation.html', context)
    
#vparticipant_adding_device.send_message("[CONTACT_EXCHANGE_DATA]" + json.dumps(participant_adding_response))


"""
Reset Password Views
"""
def forgot_password_old(request,token):
    
    try :
        
        request = ParticipantResetPassword.objects.get(token_id = token)
        p = request.participant_sent_request
        #Token is valid. Present a form to enter a new password
        
    except ParticipantResetPassword.DoesNotExist:
        context = {'status': -1}
        
    return render(request, 'password_reset.html', context)


def forgot_password(request,token):
    if request.method == 'POST':
        form = ResetPasswordForm(request.POST)
        if form.is_valid():
            # Process the data in form.cleaned_data
            # ...
            print form.cleaned_data
            data = form.cleaned_data
            u = User.objects.get(username=data['username'])
            if data['password_2'] == data['password']:
                u.set_password(data['password'])
                print "New user info %s - %s" %(u.username,u.password)
                u.save()
            return HttpResponseRedirect('/password_reset_done/') # Redirect after POST
    else:
        print token
        try :
            passReset = ParticipantResetPassword.objects.get(token_id = token)
            p = passReset.participant_sent_request
            
            #Token is valid. Present a form to enter a new password
            form = ResetPasswordForm(initial={'username' : p.username})
            validlink=True
            print "Exists"
            passReset.delete()
        except ParticipantResetPassword.DoesNotExist:
            print "Participant doesn't exist"
            validlink=False
            return render(request, 'password_reset.html',
                  {'validlink' : validlink,
                   })
        

    return render(request, 'password_reset.html',
                  {'form': form,
                   'validlink' : validlink,
                   })
        
    

# 4 views for password reset:
# - password_reset sends the mail
# - password_reset_done shows a success message for the above
# - password_reset_confirm checks the link the user clicked and
# prompts for a new password
# - password_reset_complete shows a success message for the above
"""
@csrf_protect
def password_reset(request, is_admin_site=False,
                   template_name='registration/password_reset_form.html',
                   email_template_name='registration/password_reset_email.html',
                   subject_template_name='registration/password_reset_subject.txt',
                   password_reset_form=PasswordResetForm,
                   token_generator=default_token_generator,
                   post_reset_redirect=None,
                   from_email=None,
                   current_app=None,
                   extra_context=None):
    if post_reset_redirect is None:
        post_reset_redirect = reverse('password_reset_done')
    else:
        post_reset_redirect = resolve_url(post_reset_redirect)
    if request.method == "POST":
        form = password_reset_form(request.POST)
        if form.is_valid():
            opts = {
                'use_https': request.is_secure(),
                'token_generator': token_generator,
                'from_email': from_email,
                'email_template_name': email_template_name,
                'subject_template_name': subject_template_name,
                'request': request,
            }
            if is_admin_site:
                opts = dict(opts, domain_override=request.get_host())
            form.save(**opts)
            return HttpResponseRedirect(post_reset_redirect)
    else:
        form = password_reset_form()
    context = {
        'form': form,
    }
    if extra_context is not None:
        context.update(extra_context)
    return TemplateResponse(request, template_name, context,
                            current_app=current_app)


def password_reset_done(request,
                        template_name='registration/password_reset_done.html',
                        current_app=None, extra_context=None):
    context = {}
    if extra_context is not None:
        context.update(extra_context)
    return TemplateResponse(request, template_name, context,
                            current_app=current_app)


# Doesn't need csrf_protect since no-one can guess the URL
@sensitive_post_parameters()
@never_cache
def password_reset_confirm(request, uidb36=None, token=None,
                           template_name='registration/password_reset_confirm.html',
                           token_generator=default_token_generator,
                           set_password_form=SetPasswordForm,
                           post_reset_redirect=None,
                           current_app=None, extra_context=None):
    """
"""View that checks the hash in a password reset link and presents a
form for entering a new password."""
"""
    UserModel = get_user_model()
    assert uidb36 is not None and token is not None # checked by URLconf
    if post_reset_redirect is None:
        post_reset_redirect = reverse('password_reset_complete')
    else:
        post_reset_redirect = resolve_url(post_reset_redirect)
    try:
        uid_int = base36_to_int(uidb36)
        user = UserModel._default_manager.get(pk=uid_int)
    except (ValueError, OverflowError, UserModel.DoesNotExist):
        user = None

    if user is not None and token_generator.check_token(user, token):
        validlink = True
        if request.method == 'POST':
            form = set_password_form(user, request.POST)
            if form.is_valid():
                form.save()
                return HttpResponseRedirect(post_reset_redirect)
        else:
            form = set_password_form(None)
    else:
        validlink = False
        form = None
    context = {
        'form': form,
        'validlink': validlink,
    }
    if extra_context is not None:
        context.update(extra_context)
    return TemplateResponse(request, template_name, context,
                            current_app=current_app)


def password_reset_complete(request,
                            template_name='registration/password_reset_complete.html',
                            current_app=None, extra_context=None):
    context = {
        'login_url': resolve_url(settings.LOGIN_URL)
    }
    if extra_context is not None:
        context.update(extra_context)
    return TemplateResponse(request, template_name, context,
                            current_app=current_app)


@sensitive_post_parameters()
@csrf_protect
@login_required
def password_change(request,
                    template_name='registration/password_change_form.html',
                    post_change_redirect=None,
                    password_change_form=PasswordChangeForm,
                    current_app=None, extra_context=None):
    if post_change_redirect is None:
        post_change_redirect = reverse('password_change_done')
    else:
        post_change_redirect = resolve_url(post_change_redirect)
    if request.method == "POST":
        form = password_change_form(user=request.user, data=request.POST)
        if form.is_valid():
            form.save()
            return HttpResponseRedirect(post_change_redirect)
    else:
        form = password_change_form(user=request.user)
    context = {
        'form': form,
    }
    if extra_context is not None:
        context.update(extra_context)
    return TemplateResponse(request, template_name, context,
                            current_app=current_app)


@login_required
def password_change_done(request,
                         template_name='registration/password_change_done.html',
                         current_app=None, extra_context=None):
    context = {}
    if extra_context is not None:
        context.update(extra_context)
    return TemplateResponse(request, template_name, context,
                            current_app=current_app)
"""
