exec { 'update_packages':
  command => 'apt-get update',
  path    => '/usr/bin',
}

# Install Leiningen to run tests and build project
exec { 'get_leiningen':
  command => '/usr/bin/wget https://raw.github.com/technomancy/leiningen/stable/bin/lein -O /usr/bin/lein && /bin/chmod a+x /usr/bin/lein',
  unless  => '/usr/bin/which lein &>/dev/null',
}

class { 'postgresql::server': }

postgresql::server::db { 'restful_dev':
  user     => 'restful_dev',
  password => postgresql_password('restful_dev', 'pass_dev'),
}

postgresql::server::db { 'restful_test':
  user     => 'restful_test',
  password => postgresql_password('restful_test', 'pass_test'),
}

file { "/etc/profile.d/env.sh":
  content => "export RESTFUL_DB_URL=\"jdbc:postgresql://localhost:5432/restful_dev?user=restful_dev&password=pass_dev\""
}

include java7

