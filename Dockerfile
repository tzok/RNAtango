FROM alpine:3.18.3
RUN  apk update \
  && apk upgrade \
  && apk add ca-certificates \
  && update-ca-certificates \
  && apk add openjdk17 \
  && apk add maven \
  && apk add git \
  && apk add bash \ 
  && apk add --update ttf-dejavu && rm -rf /var/cache/apk/*

RUN mkdir -p /opt/rnatango
COPY ./docker-entrypoint.sh /opt/rnatango
WORKDIR /opt/rnatango
COPY . /opt/rnatango
WORKDIR /opt/
RUN git clone https://github.com/tzok/mcq4structures/
RUN git clone https://github.com/tzok/varna-tz
WORKDIR /opt/mcq4structures/mcq-core 
RUN mvn install
WORKDIR /opt/varna-tz 
RUN mvn install
WORKDIR /opt/rnatango/engine
#ENTRYPOINT ["mvn", "spring-boot:run"]
