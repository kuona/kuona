module.exports = function (grunt) {
  grunt.initConfig({
    "babel": {
      options: {
        sourceMap: true
      },
      dist: {
        files: [
          {
            expand: true,
            cwd: 'src/js',
            src: ['**/*.js'],
            dest: 'out/js'
          }
        ]
      }
    },
    watch: {
      data: {
        files: ['src/**', 'lib/**', '!src/sass'],
        tasks: ['copy']
      },
      sass: {
        files: ['src/sass/**'],
        tasks: ['compass']
      }
    },
    copy: {
      main: {
        expand: true,
        cwd: 'src/',
        src: '**',
        dest: 'out/',
      },
      lib: {
        expand: true,
        cwd: 'lib/',
        src: '**',
        dest: 'out/',
      }
    },
    clean: {
      build: {
        src: ['out']
      }
    },
    browserSync: {
      options: {
        port: 4000,
        middleware: [require('http-proxy-middleware')('/api', {target: 'http://localhost:8080'})]
      },
      dev: {
        bsFiles: {
          src: [
            'out/**'
          ]
        },
        options: {
          watchTask: true,
          server: './out'
        }
      }
    },
    sass: {
      dist: {
        files: {
          'out/css/dashboard.css': 'src/sass/dashboard.sass'
        }
      }
    }
  });

  grunt.loadNpmTasks('grunt-contrib-clean');
  require('load-grunt-tasks')(grunt);

//  grunt.registerTask("default", ["babel"]);
  grunt.registerTask('build', ['clean', 'copy', 'sass', 'babel']);
  grunt.registerTask('dev', ['build', 'browserSync', 'watch']);
  grunt.registerTask('default', ['browserify:admin', 'build']);
};
