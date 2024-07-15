from elasticsearch import Elasticsearch

USERNAME = "elastic"
PASSWORD = "R84G7m0BU2r6w10Z4ICNG2mE"


if __name__ == '__main__':
    es = Elasticsearch("http://localhost:9200",
                       basic_auth=(USERNAME, PASSWORD),
                       api_key='hIq2Tuo4RXmYkWVpYzlT8A',
                       ca_certs='../es-ca.crt',
                       verify_certs=True)
    es.info()