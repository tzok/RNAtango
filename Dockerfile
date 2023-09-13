FROM alpine:3.18.3
RUN  apk update \
  && apk upgrade \
  && apk add ca-certificates \
  && update-ca-certificates \
  && apk add openjdk17 \
  && apk add maven

RUN mkdir -p /opt/rnatango
COPY . /opt/rnatango
WORKDIR /opt/rnatango/engine
ENTRYPOINT ["mvn", "install"]
