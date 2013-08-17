'''
Created on 10 de Jun de 2013

@author: David
'''
import hashlib
from tastypie.authentication import Authentication
from tastypie.authorization import Authorization
from django.contrib.auth.models import User
from django.contrib.auth.hashers import MD5PasswordHasher
from django.core.exceptions import ObjectDoesNotExist
import json
import random
import hashlib
from django.utils.datastructures import SortedDict
from django.utils.encoding import force_bytes, force_str, force_text
from django.core.exceptions import ImproperlyConfigured
from django.utils.crypto import (
    pbkdf2, constant_time_compare, get_random_string)

class CustomAuthentication(Authentication):
    def is_authenticated(self, request, **kwargs):
        if request.method == "GET":
            params = request.GET
        elif request.method == "POST":
            params = json.loads(request.body)
            
        if params != None :
            uname =  params['user_name']
            encodedpw = params['password']
            if (uname != None) and (encodedpw != None) :
                try:                        user = User.objects.get(username=uname)
                except ObjectDoesNotExist:
                    return False
    
                if encodedpw == user.password :
                    print "OK"
                    return True
            
        return False
    
    def validate_user(self, request, username,password):
        try:
            user = User.objects.get(username=username)
        except ObjectDoesNotExist:
            return False
        
        if password == user.password :
            print "OK"
            return True
        else:
            return False
    
    # Optional but recommended
    def get_identifier(self, request):
        return request.user.username
    
    
    
    
    
class ParticipantAuthorization(Authorization):
    def is_authorized(self, request, object=None):
        return True
        if request.user.date_joined.year == 2010:
            return True
        else:
            return True

    # Optional but useful for advanced limiting, such as per user.
    def apply_limits(self, request, object_list):
        print object_list
        if request and hasattr(request, 'user'):
            return object_list.filter(author__username=request.user.username)

        return object_list.none()
    
    
class MD5Hasher():
    """
    The Salted MD5 password hashing algorithm (not recommended)
    """
    algorithm = "md5"

    def encode(self, password, salt):
        assert password is not None
        assert salt and '$' not in salt
        hash = hashlib.md5(force_bytes(salt + password)).hexdigest()
        return hash
        return hash

    def verify(self, password, encoded):
        algorithm, salt, hash = encoded.split('$', 2)
        assert algorithm == self.algorithm
        encoded_2 = self.encode(password, salt)
        return constant_time_compare(encoded, encoded_2)





def createSaltedMD5Hash(originalString, saltSize = 6):
    ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
    chars = []
    for i in range(saltSize):
        chars.append(random.choice(ALPHABET))
    
    #completeString = originalString.join(chars)
    
    md5 = MD5Hasher()
    return md5.encode(originalString,"".join(chars))

        
        

