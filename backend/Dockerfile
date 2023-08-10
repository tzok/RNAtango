FROM node:18

ENV PYTHONUNBUFFERED 1
RUN apt-get update && apt-get install -y python3-pip curl supervisor uvicorn
RUN curl -sL https://deb.nodesource.com/setup_14.x | bash -
RUN apt-get -y install nodejs 
RUN mkdir -p /opt/rnatango
WORKDIR /opt/rnatango
COPY . /opt/rnatango

#RUN chmod a+x frontend/compile_less.sh
COPY build/ws_supervisor.conf /etc/supervisor/conf.d/
COPY build/web_supervisor.conf /etc/supervisor/conf.d/
RUN pip3 install -r requirements.txt

WORKDIR /opt/rnatango
