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
RUN echo '<?xml version="1.0" encoding="UTF-8" standalone="no"?><svg
        xmlns:dc="http://purl.org/dc/elements/1.1/"
        xmlns:cc="http://creativecommons.org/ns#"
        xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        xmlns:sodipodi="http://sodipodi.sourceforge.net/DTD/sodipodi-0.dtd"
        xmlns:inkscape="http://www.inkscape.org/namespaces/inkscape"
        xmlns="http://www.w3.org/2000/svg"
        id="svg4938"
        version="1.1"
        viewBox="0 0 17.85704 27.001228"
        height="27.001228mm"
        width="17.85704mm"
        sodipodi:docname="mcq-legend.svg"
        inkscape:version="0.92.4 5da689c313, 2019-01-14"></svg>' > /opt/mcq4structures/mcq-core/src/main/resources/mcq-legend.svg
RUN mvn install
WORKDIR /opt/varna-tz 
RUN mvn install

RUN mvn install:install-file -Dfile=/opt/mcq4structures/mcq-cli/target/mcq-cli-1.8.3.jar -DgroupId=pl.poznan.put.mcq -DartifactId=mcq-core -Dversion=1.8.3 -Dpackaging=jar
RUN mvn install:install-file -Dfile=/opt/mcq4structures/mcq-clustering/target/mcq-clustering-1.8.3.jar -DgroupId=pl.poznan.put.mcq -DartifactId=mcq-clustering -Dversion=1.8.3 -Dpackaging=jar
RUN mvn install:install-file -Dfile=/opt/varna-tz/target/varna-tz-1.2.1.jar -DgroupId=com.github.tzok -DartifactId=varna-tz -Dversion=1.2.1 -Dpackaging=jar
WORKDIR /opt/rnatango/engine
#ENTRYPOINT ["mvn", "spring-boot:run"]
