from django.shortcuts import render
from django.http import HttpResponse
from django.views.decorators.csrf import ensure_csrf_cookie
from rnatango.settings import FRONTEND_LOCATION

@ensure_csrf_cookie
def index(request):
    try:
        with open(FRONTEND_LOCATION) as react_frontend:
            return HttpResponse(content=bytes(react_frontend.read(), "UTF-8"))
    except FileNotFoundError:
        return render(request, "server_starting.html")
