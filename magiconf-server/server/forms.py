'''
Created on 21 de Jun de 2013

@author: David
'''
from django import forms

class ResetPasswordForm(forms.Form):
    password = forms.CharField(max_length=32, widget=forms.PasswordInput,label='New Password') 
    password_2 = forms.CharField(max_length=32, widget=forms.PasswordInput,label='Password (again)')
    username = forms.CharField(max_length=32,widget=forms.HiddenInput) 