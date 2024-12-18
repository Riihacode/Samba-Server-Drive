KONFIGURASI SAMBA SERVER FILE SHARING di Ubuntu Virtual Box (sebagai Virtual Machine)
1. Network harus disetting menjadi Bridged Network (Sebelum run VM Ubuntu)
2. sudo apt update
3. sudo apt install samba
4. sudo systemctl status smbd
5. sudo mkdir -p /srv/samba_office/division_a		(pembuatan direktori division_a di dalam samba_office)
6. sudo mkdir -p /srv/samba_office/division_b		(pembuatan direktori division_b di dalam samba_office)

7. sudo chmod 2775 /srv/samba_office/division_a
8. sudo chown nobody:nogroup /srv/samba_office/division_a

9. sudo chmod 2775 /srv/samba_office/division_b
10. sudo chown nobody:nogroup /srv/samba_office/division_b

11. sudo nano /etc/samba/smb.confs
[samba_office]
        path = /srv/samba_office
        writeable = yes
        browsable = yes
        guest ok = yes
        read only = no

	  #Yang ini untuk di bagian Aplikasi Android 
        # Tambahan untuk memastikan izin folder baru
        # Izin default untuk file baru  
        create mask = 0777
        # Izin default untuk folder baru        
        directory mask = 0777
        # Memaksa izin untuk file baru
        force create mode = 0777
        # Memaksa izin untuk folder baru
        force directory mode = 0777


12. sudo systemctl restart smbd		(untuk restart server samba & menjalankan samba server; kalau ingin inisialisasi pertama kali `restart` diganti start; klo ingin stop `restart` diganti stop)	
13. smbclient -L  localhost -N
