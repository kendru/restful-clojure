# -*- mode: ruby -*-
# vi: set ft=ruby :

# Vagrantfile API/syntax version. Don't touch unless you know what you're doing!
VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
	# We're using an Ubuntu box from Puppet Labs with Puppet already installed
	config.vm.box = "ubuntu-puppetlabs"

	# The url from where the 'config.vm.box' box will be fetched if it
	# doesn't already exist on the user's system.
	config.vm.box_url = "http://puppet-vagrant-boxes.puppetlabs.com/ubuntu-server-12042-x64-vbox4210.box"

	# Create a private network so that we can access our VM like any other maching
	# on our network. We could use port forwarding instead, but we're opting to
	# access the VM as a separate machine to mimick a more production-like setup.
	config.vm.network :private_network, ip: "192.168.33.10"

	# Create bridged network so that our VM can access the internet through the
	# host machine's network.
	config.vm.network :public_network

	config.vm.synced_folder "./restful-clojure", "/vagrant"

	# Provider-specific configuration so you can fine-tune various
	# backing providers for Vagrant. These expose provider-specific options.
	# Example for VirtualBox:
	#
	# config.vm.provider :virtualbox do |vb|
	#   # Don't boot with headless mode
	#   vb.gui = true
	#
	#   # Use VBoxManage to customize the VM. For example to change memory:
	#   vb.customize ["modifyvm", :id, "--memory", "1024"]
	# end
	#
	# View the documentation for the provider you're using for more
	# information on available options.

	config.vm.provision "puppet" do |puppet|
		puppet.manifests_path = "puppet/manifests"
		puppet.module_path = "puppet/modules"
		puppet.manifest_file = "default.pp"
	end
end
