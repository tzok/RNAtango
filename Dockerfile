FROM alpine:3.18.3
RUN  apk update \
  && apk upgrade \
  && apk add ca-certificates \
  && update-ca-certificates \
  && apk add openjdk17 \
  && apk add maven \
  && apk add git \
  && apk add bash \ 
  && apk add build-base \
  && apk add libc6-compat \ 
  && apk add python3 \ 
  && apk add py3-pip \ 
  && apk add --update ttf-dejavu && rm -rf /var/cache/apk/*

RUN mkdir -p /opt/rnatango
COPY ./docker-entrypoint.sh /opt/rnatango
WORKDIR /opt/rnatango
COPY . /opt/rnatango
RUN pip3 install -r requirements.txt
WORKDIR /opt/
RUN git clone https://github.com/tzok/mcq4structures/
RUN git clone https://github.com/tzok/varna-tz
WORKDIR /opt/mcq4structures 
RUN mvn install
WORKDIR /opt/varna-tz 
RUN mvn install

RUN mvn install:install-file -Dfile=/opt/mcq4structures/mcq-cli/target/mcq-cli-1.8.2.jar -DgroupId=pl.poznan.put.mcq -DartifactId=mcq-core -Dversion=1.8.2 -Dpackaging=jar
RUN mvn install:install-file -Dfile=/opt/mcq4structures/mcq-clustering/target/mcq-clustering-1.8.2.jar -DgroupId=pl.poznan.put.mcq -DartifactId=mcq-clustering -Dversion=1.8.2 -Dpackaging=jar
RUN mvn install:install-file -Dfile=/opt/varna-tz/target/varna-tz-1.2.1.jar -DgroupId=com.github.tzok -DartifactId=varna-tz -Dversion=1.2.1 -Dpackaging=jar
WORKDIR /opt/rnatango/engine
#ENTRYPOINT ["mvn", "spring-boot:run"]
