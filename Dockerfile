FROM debian:sid

RUN apt-get update && apt-get install -y default-jdk

COPY src /var/work-src

WORKDIR /var/work-src

RUN javac Main.java

COPY cfp.sh cfp.sh

ENV REMOVE_DEV_RANDOM=true

ENTRYPOINT [ "bash" ]
CMD [ "./cfp.sh" ]
